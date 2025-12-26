package traversium.tripservice.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.grpc.client")
class GrpcProperties(
    var moderation: ServerConfig = ServerConfig(host = "localhost", port = 9090),
)

class ServerConfig(
    var host: String = "localhost",
    var port: Int = 9090,
)