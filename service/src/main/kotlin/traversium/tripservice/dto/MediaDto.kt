package traversium.tripservice.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import com.fasterxml.jackson.annotation.JsonIgnore
import traversium.tripservice.db.model.Media
import java.time.OffsetDateTime

data class MediaDto(
    val mediaId: Long?,
    val pathUrl: String? = null,
    val uploader: String?,
    val fileType: String? = null,
    val fileFormat: String? = null,
    val fileSize: Long? = null,
    val geoLocation: GeoLocation? = null,
    val createdAt : OffsetDateTime? = null,

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val uploadedAt: OffsetDateTime? = null,
) {
    fun toMedia(): Media = Media(
        mediaId = mediaId,
        pathUrl = pathUrl ?: "",
        uploader = uploader ?: throw IllegalArgumentException("Uploader must be set"),
        fileType = fileType ?: "",
        fileFormat = fileFormat ?: "",
        fileSize = fileSize ?: 0L,
        geoLocation = geoLocation ?: GeoLocation.UNKNOWN,
        createdAt = createdAt ?: Media.DEFAULT_DATE
    )
}

@Embeddable
data class GeoLocation(
    @Column(name = "latitude")
    val latitude: Double = 0.0,
    @Column(name = "longitude")
    val longitude: Double = 0.0
){
    companion object {
        val UNKNOWN = GeoLocation()
    }

    @JsonIgnore
    fun hasUnknownCoordinates(): Boolean =
        latitude == 0.0 && longitude == 0.0
}