package traversium.tripservice.kafka.data

import java.time.Instant

data class TripEvent(
    val eventType: TripEventType,
    val timestamp: Instant = Instant.now(),
    val tripId: Long?,
    val owner: String,
)
