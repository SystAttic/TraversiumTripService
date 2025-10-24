package traversium.tripservice.dto

import traversium.tripservice.db.model.Album

data class AlbumDto(
    val albumId: Long?,
    val title: String?,
    val description: String? = null,
    //val tripId: Long?,
    val media: Set<MediaDto> = emptySet()
) {
    fun toAlbum(): Album = Album(
        albumId = albumId,
        title = title,
        description = description,
        media = media.map { it.toMedia() }.toMutableSet()
    )
}
