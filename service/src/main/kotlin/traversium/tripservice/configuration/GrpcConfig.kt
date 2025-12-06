package traversium.tripservice.configuration

import io.grpc.ServerInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.server.ServerBuilderCustomizer
import traversium.tripservice.security.FirebaseGrpcInterceptor

@Configuration
class GrpcConfig {

    @Bean
    fun grpcServerBuilderCustomizer(
        firebaseGrpcInterceptor: FirebaseGrpcInterceptor
    ): ServerBuilderCustomizer<*> {
        return ServerBuilderCustomizer { serverBuilder ->
            serverBuilder.intercept(firebaseGrpcInterceptor)
        }
    }
}
