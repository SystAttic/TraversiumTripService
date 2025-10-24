package traversium.tripservice.dto

import traversium.tripservice.db.model.Trip

data class TripDto(
    val tripId: Long?,
    val title: String,
    val description: String? = null,
    val ownerId: String,
    val coverPhotoUrl: String? = null,
    val collaborators: Set<String> = emptySet(),
    val viewers: Set<String> = emptySet(),
    val albums: Set<AlbumDto> = emptySet(),
) {
    fun toTrip() = Trip(
        tripId = tripId,
        title = title,
        description = description,
        ownerId = ownerId,
        coverPhotoUrl = coverPhotoUrl,
        collaborators = collaborators,
        viewers = viewers,
        albums = albums.map { it.toAlbum() }.toMutableSet(),
    )
}