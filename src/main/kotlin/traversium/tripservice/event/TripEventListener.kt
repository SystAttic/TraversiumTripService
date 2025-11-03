package traversium.tripservice.event

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import traversium.tripservice.kafka.KafkaProperties
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.MediaEvent
import traversium.tripservice.kafka.data.TripEvent
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