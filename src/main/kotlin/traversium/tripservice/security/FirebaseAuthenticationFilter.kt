package traversium.tripservice.security


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import traversium.tripservice.service.FirebaseService
import traversium.tripservice.service.TenantService

@Component
class FirebaseAuthenticationFilter(
    private val firebaseService: FirebaseService,
    private val tenantService: TenantService
) : OncePerRequestFilter(){
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain) {
        try {
            val authHeader = request.getHeader("Authorization")
            if (authHeader == null) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }
            val token = authHeader.removePrefix("Bearer ").trim()

            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
            val uid = decodedToken.uid

            SecurityContextHolder.getContext().authentication = TraversiumAuthentication(
                userRecordToPrincipal(FirebaseAuth.getInstance().getUser(uid)),
                null,
                emptyList(),
                token
            )

            val tenantId = firebaseService.extractTenantIdFromToken(token)
            tenantService.setCurrentTenant(tenantId)

            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.message)
        } finally {
            tenantService.clear()
        }
    }

    private fun userRecordToPrincipal(userRecord: UserRecord): TraversiumPrincipal = TraversiumPrincipal(
        userRecord.uid,
        userRecord.email,
        userRecord.photoUrl
    )
}