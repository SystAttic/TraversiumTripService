package traversium.tripservice.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import traversium.moderation.textmoderation.TextModerationServiceGrpc

@RefreshScope
@EnableConfigurationProperties(GrpcProperties::class)
@Configuration
class GrpcClientConfig(
    private val grpcProperties: GrpcProperties
) {
    @Bean (name = ["moderationGrpcChannel"])
    fun moderationGrpcChannel(): ManagedChannel =
        ManagedChannelBuilder
            .forAddress(grpcProperties.moderation.host, grpcProperties.moderation.port)
            .usePlaintext()
            .build()

    @Bean
    fun moderationStub(moderationGrpcChannel: ManagedChannel): TextModerationServiceGrpc.TextModerationServiceBlockingStub =
        TextModerationServiceGrpc.newBlockingStub(moderationGrpcChannel)

}