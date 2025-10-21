package traversium.tripservice.dto

import traversium.tripservice.db.model.Trip

data class TripDto(
    val tripId: Long,
    val name: String,
    val coverPhoto: String? = null,
    val owner: String,
    val editors: Set<String>? = null,
    val viewers: Set<String>? = null,
    val albums: List<Long>? = null,
) {
    fun toTrip() = Trip(
        tripId = tripId,
        name = name,
        coverPhoto = coverPhoto,
        owner = owner,
        editors = editors,
        viewers = viewers,
        albums = albums
    )
}