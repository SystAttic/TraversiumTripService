package traversium.tripservice.event

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import traversium.tripservice.kafka.KafkaProperties
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.MediaEvent
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.UserBlockedEvent
import traversium.tripservice.service.TripCleanupService
import java.util.concurrent.TimeUnit

/**
 * @author Traversium
 */
@Component
@ConditionalOnProperty(prefix = "kafka", name = ["bootstrap-servers"])
class TripEventListener(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaProperties: KafkaProperties
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendTripReportingDataToKafka(event: TripEvent) {
        kafkaTemplate.send(kafkaProperties.reportingTopic, event)
            .get(kafkaProperties.clientConfirmationTimeout, TimeUnit.SECONDS)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendAlbumReportingDataToKafka(event: AlbumEvent) {
        kafkaTemplate.send(kafkaProperties.reportingTopic, event)
            .get(kafkaProperties.clientConfirmationTimeout, TimeUnit.SECONDS)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendMediaReportingDataToKafka(event: MediaEvent) {
        kafkaTemplate.send(kafkaProperties.reportingTopic, event)
            .get(kafkaProperties.clientConfirmationTimeout, TimeUnit.SECONDS)
    }
}
@Component
class UserBlockListener(
    private val tripCleanupService: TripCleanupService
) {

    @KafkaListener(topics = ["user-block-events"], groupId = "trip-service")
    fun handleUserBlocked(event: UserBlockedEvent) {
        println("Received user-block event: $event")
        tripCleanupService.removeBlockedUserRelations(event.blockerId, event.blockedId)
    }
}