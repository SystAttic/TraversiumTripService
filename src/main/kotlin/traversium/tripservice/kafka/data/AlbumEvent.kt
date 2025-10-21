package traversium.tripservice.kafka.data

import java.time.Instant

data class AlbumEvent(
    val eventType: AlbumEventType,
    val timestamp: Instant = Instant.now(),
    val albumId: Long,
    val tripId: Long
)
