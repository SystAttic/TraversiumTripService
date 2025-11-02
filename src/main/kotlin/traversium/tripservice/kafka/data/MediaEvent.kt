package traversium.tripservice.kafka.data

import java.time.Instant

data class MediaEvent(
    val eventType: MediaEventType,
    val timestamp: Instant = Instant.now(),
    val mediaId: Long?,
    val pathUrl: String?
)
