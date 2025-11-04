package traversium.tripservice.dto

import com.fasterxml.jackson.annotation.JsonProperty
import traversium.tripservice.db.model.Trip
import traversium.tripservice.db.model.Visibility
import java.time.Instant

data class TripDto(
    val tripId: Long?,
    val title: String?,
    val description: String? = null,
    val ownerId: String?,
    val visibility: Visibility? = null,
    val coverPhotoUrl: String? = null,
    val collaborators: List<String> = emptyList(),
    val viewers: List<String> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdAt : Instant = Instant.now(),
) {
    fun toTrip() = Trip(
        tripId = tripId,
        title = title,
        description = description ?: "",
        ownerId = ownerId,
        visibility = visibility ?: Visibility.PRIVATE,
        coverPhotoUrl = coverPhotoUrl ?: "",
        collaborators = collaborators.toMutableList(),
        viewers = viewers.toMutableList(),
        albums = albums.map { it.toAlbum() }.toMutableList(),
    )
}