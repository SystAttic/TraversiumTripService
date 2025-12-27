package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.GeoLocation
import traversium.tripservice.dto.MediaDto
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
@Table(name = Media.TABLE_NAME)
data class Media(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id", unique = true, nullable = false, updatable = false, length = 36)
    val mediaId: Long? = null,

    @Column(name= "path_url")
    val pathUrl: String? = null, // reference from File Storage Service

    @Column(name="uploader")
    val uploader: String? = null, // who uploaded

    @Column(name="file_type")
    val fileType: String? = null, // image | video

    @Column(name="file_format")
    val fileFormat: String? = null, // e.g. jpg, png, mp4

    @Column(name="file_size")
    val fileSize: Long? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "latitude", column = Column(name = "latitude")),
        AttributeOverride(name = "longitude", column = Column(name = "longitude"))
    )
    val geoLocation: GeoLocation = GeoLocation.UNKNOWN, // will store coordinates

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = DEFAULT_DATE,

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    var uploadedAt: OffsetDateTime? = null
) {
    companion object {
        const val TABLE_NAME = "media"
        val DEFAULT_DATE: OffsetDateTime = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
    }

    @PrePersist
    protected fun onCreate() {
//        if (createdAt == null) {
//            createdAt = OffsetDateTime.now() // Only set for new entity
//        }
        if (uploadedAt == null) {
            uploadedAt = OffsetDateTime.now() // Only set for new entity
        }
    }

    fun toDto(): MediaDto = MediaDto(
        mediaId = mediaId,
        pathUrl = pathUrl,
        uploader = uploader,
        fileType = fileType,
        fileFormat = fileFormat,
        fileSize = fileSize,
        geoLocation = geoLocation,
        createdAt = createdAt,
        uploadedAt = uploadedAt
    )

}
