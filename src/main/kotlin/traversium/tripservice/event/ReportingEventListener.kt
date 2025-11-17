package traversium.tripservice.event

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import traversium.tripservice.kafka.KafkaProperties
import traversium.tripservice.kafka.data.ReportingStreamData
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnProperty(prefix = "kafka", name = ["bootstrap-servers"])
class TripEventListener(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val kafkaProperties: KafkaProperties
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun sendReportingDataToKafka(event: ReportingStreamData) {
        kafkaTemplate.send(
            kafkaProperties.reportingTopic,
            event
        )[kafkaProperties.clientConfirmationTimeout, TimeUnit.SECONDS]
    }
}