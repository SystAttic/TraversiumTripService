package traversium.tripservice.kafka.data

import java.time.OffsetDateTime

data class TripEvent(
    val eventType: TripEventType,
    override val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val tripId: Long?,
    val ownerId: String?,
) : DomainEvent
