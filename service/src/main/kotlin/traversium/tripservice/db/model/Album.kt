package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.AlbumDto
import java.time.Instant
import java.time.OffsetDateTime

@Entity
@Table(name = Album.TABLE_NAME)
data class Album(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id", unique = true, nullable = false, updatable = false, length = 36)
    var albumId: Long? = null,

    @Column(name="title", nullable = false)
    var title: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
        name = "album_media",
        joinColumns = [JoinColumn(name = "album_id")],
        inverseJoinColumns = [JoinColumn(name = "media_id")],
        indexes = [
            Index(name = "idx_album_media_albums", columnList = "album_id"),
            Index(name = "idx_album_media_media", columnList = "media_id")
        ]
    )    var media: MutableList<Media> = mutableListOf()
) {
    companion object {
        const val TABLE_NAME = "album"
    }

    @PrePersist
    protected fun onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now() // Only set for new entity
        }
    }

    fun toDto(): AlbumDto = AlbumDto(
        albumId = albumId,
        title = title,
        description = description,
        media = media.map { it.toDto() }.toMutableList(),
        createdAt = createdAt ?: OffsetDateTime.now()
    )

}
