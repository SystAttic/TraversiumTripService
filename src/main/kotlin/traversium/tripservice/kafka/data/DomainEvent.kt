package traversium.tripservice.kafka.data

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TripEvent::class, name = "TripEvent"),
    JsonSubTypes.Type(value = AlbumEvent::class, name = "AlbumEvent"),
    JsonSubTypes.Type(value = MediaEvent::class, name = "MediaEvent")
)
sealed interface DomainEvent {
    val timestamp: OffsetDateTime
}