package traversium.tripservice.dto

import traversium.tripservice.db.model.Media
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class MediaDto(
    val mediaId: Long?,
    val pathUrl: String? = null,
    val ownerId: String,
    val fileType: String? = null,
    val fileFormat: String? = null,
    val fileSize: Long? = null,
    val geoLocation: String? = null,
    val createdAt: OffsetDateTime? = null,
) {
    fun toMedia(): Media = Media(
        mediaId = mediaId,
        pathUrl = pathUrl ?: "",
        ownerId = ownerId,
        fileType = fileType ?: "",
        fileFormat = fileFormat ?: "",
        fileSize = fileSize ?: 0L,
        geoLocation = geoLocation ?: "",
        createdAt = createdAt ?: OffsetDateTime.now(ZoneOffset.UTC)
    )
}
