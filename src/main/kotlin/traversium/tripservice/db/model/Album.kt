package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.AlbumDto

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

    @OneToMany(mappedBy = "album", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var media: MutableList<Media> = mutableListOf()
) {
    companion object {
        const val TABLE_NAME = "album"
    }

    fun toDto(): AlbumDto = AlbumDto(
        albumId = albumId,
        title = title,
        description = description,
        media = media.map { it.toDto() }.toMutableList()
    )

}
