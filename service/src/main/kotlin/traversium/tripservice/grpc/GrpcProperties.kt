package traversium.tripservice.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.grpc.client.moderation-service")
class GrpcProperties(
    val host: String,
    val port: Int,
)