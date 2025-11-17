package traversium.tripservice.kafka.data

import java.time.OffsetDateTime

data class MediaEvent(
    val eventType: MediaEventType,
    override val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val mediaId: Long?,
    val pathUrl: String?
) : DomainEvent
