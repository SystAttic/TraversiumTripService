package traversium.tripservice.dto

import traversium.tripservice.db.model.Media
import java.time.OffsetDateTime

data class MediaDto(
    val mediaId: Long?,
    val pathUrl: String? = null,
    val uploader: String,
    val fileType: String? = null,
    val fileFormat: String? = null,
    val fileSize: Long? = null,
    val geoLocation: String? = null,
    val createdAt: OffsetDateTime? = null,
) {
    fun toMedia(): Media = Media(
        mediaId = mediaId,
        pathUrl = pathUrl ?: "",
        uploader = uploader,
        fileType = fileType ?: "",
        fileFormat = fileFormat ?: "",
        fileSize = fileSize ?: 0L,
        geoLocation = geoLocation ?: "",
        createdAt = createdAt ?: OffsetDateTime.now()
    )
}
