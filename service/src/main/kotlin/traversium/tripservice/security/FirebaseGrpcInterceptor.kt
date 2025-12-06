package traversium.tripservice.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import io.grpc.*
import org.apache.logging.log4j.kotlin.Logging
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import traversium.commonmultitenancy.TenantContext
import traversium.commonmultitenancy.TenantUtils

@Component
class
FirebaseGrpcInterceptor(
    private val firebaseAuth: FirebaseAuth,
) : ServerInterceptor, Logging {

    companion object {
        private val AUTHORIZATION_METADATA_KEY: Metadata.Key<String> =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)
    }

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        try {
            val authHeader = headers.get(AUTHORIZATION_METADATA_KEY)
            if (authHeader == null) {
                call.close(
                    Status.UNAUTHENTICATED.withDescription("Missing Authorization metadata"),
                    Metadata()
                )
                return object : ServerCall.Listener<ReqT>() {}
            }

            val token = authHeader.removePrefix("Bearer ").trim()

            val decodedToken = firebaseAuth.verifyIdToken(token)
            val uid = decodedToken.uid
            val tenantId = decodedToken.tenantId

            TenantContext.setTenant(TenantUtils.sanitizeTenantIdForSchema(tenantId ?: "public"))

            val userRecord = if (tenantId != null) {
                try {
                    val tenantAuth = firebaseAuth.tenantManager.getAuthForTenant(tenantId)
                    tenantAuth.getUser(uid)
                } catch (e: FirebaseAuthException) {
                    logger.error("Failed to get user from tenant $tenantId: ${e.message}")
                    call.close(
                        Status.UNAUTHENTICATED.withDescription("Invalid tenant: ${e.message}"),
                        Metadata()
                    )
                    return object : ServerCall.Listener<ReqT>() {}
                }
            } else {
                firebaseAuth.getUser(uid)
            }

            SecurityContextHolder.getContext().authentication = TraversiumAuthentication(
                userRecordToPrincipal(userRecord),
                null,
                emptyList(),
                token
            )

            logger.debug("Authenticated gRPC request for user ${userRecord.email} in tenant $tenantId")

            return next.startCall(call, headers)
        } catch (ex: Exception) {
            logger.error("gRPC authentication failed: ${ex.message}", ex)
            call.close(
                Status.UNAUTHENTICATED.withDescription("Authentication failed: ${ex.message}"),
                Metadata()
            )
            return object : ServerCall.Listener<ReqT>() {}
        }
    }

    private fun userRecordToPrincipal(userRecord: UserRecord): TraversiumPrincipal = TraversiumPrincipal(
        userRecord.uid,
        userRecord.email,
        userRecord.photoUrl
    )
}
