package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.MediaDto

@Entity
@Table(name = Media.TABLE_NAME)
data class Media(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val mediaId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    val album: Album? = null,

    @Column(nullable = false)
    val pathUrl: String, // reference from File Storage Service

    @Column(nullable = false)
    val ownerId: String, // who uploaded

    @Column(nullable = false)
    val fileType: String, // image | video

    @Column(nullable = false)
    val fileFormat: String, // e.g. jpg, png, mp4

    @Column(nullable = false)
    val fileSize: Long,

    val geoLocation: String? = null, // will later store coordinates or JSON
    val timeCreated: String? = null  // ISO string from metadata or upload time
) {
    companion object {
        const val TABLE_NAME = "media"
    }

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
