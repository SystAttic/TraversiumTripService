package traversium.tripservice.dto

import traversium.tripservice.db.model.Media

data class MediaDto(
    val mediaId: Long?,
    val pathUrl: String,
    val ownerId: String,
    val fileType: String,
    val fileFormat: String,
    val fileSize: Long,
    val geoLocation: String? = null,
    val timeCreated: String? = null
) {
    fun toMedia(): Media = Media(
        mediaId = mediaId,
        pathUrl = pathUrl,
        ownerId = ownerId,
        fileType = fileType,
        fileFormat = fileFormat,
        fileSize = fileSize,
        geoLocation = geoLocation,
        timeCreated = timeCreated
    )
}
