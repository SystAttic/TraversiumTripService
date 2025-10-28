package traversium.tripservice.dto

import traversium.tripservice.db.model.Trip

data class TripDto(
    val tripId: Long?,
    val title: String?,
    val description: String? = null,
    val ownerId: String?,
    val coverPhotoUrl: String? = null,
    val collaborators: List<String> = emptyList(),
    val viewers: List<String> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
) {
    fun toTrip() = Trip(
        tripId = tripId,
        title = title,
        description = description,
        ownerId = ownerId,
        coverPhotoUrl = coverPhotoUrl,
        collaborators = collaborators.toMutableList(),
        viewers = viewers.toMutableList(),
        albums = albums.map { it.toAlbum() }.toMutableList(),
    )
}