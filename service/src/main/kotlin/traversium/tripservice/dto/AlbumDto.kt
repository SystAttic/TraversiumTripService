package traversium.tripservice.dto

import com.fasterxml.jackson.annotation.JsonProperty
import traversium.tripservice.db.model.Album
import java.time.OffsetDateTime

data class AlbumDto(
    val albumId: Long?,
    val title: String?,
    val description: String? = null,
    val media: List<MediaDto> = emptyList(),

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdAt : OffsetDateTime? = null,
) {
    fun toAlbum(): Album = Album(
        albumId = albumId,
        title = title,
        description = description ?: "",
        media = media.map { it.toMedia() }.toMutableList()
    )
}
