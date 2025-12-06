package traversium.tripservice.kafka.data

import java.time.OffsetDateTime

data class AlbumEvent(
    val eventType: AlbumEventType,
    override val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val albumId: Long?,
    val title: String?,
) : DomainEvent
