package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.MediaDto

@Entity
@Table(name = Media.TABLE_NAME)
data class Media(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id", unique = true, nullable = false, updatable = false, length = 36)
    val mediaId: Long? = null,

    @Column(name= "path_url")
    val pathUrl: String?, // reference from File Storage Service

    @Column(name="owner_id")
    val ownerId: String?, // who uploaded

    @Column(name="file_type")
    val fileType: String?, // image | video

    @Column(name="file_format")
    val fileFormat: String?, // e.g. jpg, png, mp4

    @Column(name="file_size")
    val fileSize: Long?,

    @Column(name="geo_location")
    val geoLocation: String? = null, // will later store coordinates or JSON

    @Column(name="time_created")
    val timeCreated: String? = null  // ISO string from metadata or upload time
) {
    companion object {
        const val TABLE_NAME = "media"
    }

    constructor() : this(null, null, null, null, null, null, null, null) // ✅ Hibernate-friendly constructor

    fun toDto(): MediaDto = MediaDto(
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
