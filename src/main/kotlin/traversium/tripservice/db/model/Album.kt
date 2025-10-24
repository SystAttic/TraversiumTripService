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

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    val trip: Trip? = null,*/

    @Column(name="title", nullable = false)
    val title: String,

    @Column(name = "description")
    val description: String? = null,

    @OneToMany(mappedBy = "album", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val media: MutableSet<Media> = mutableSetOf()
) {
    companion object {
        const val TABLE_NAME = "album"
    }

    fun toDto(): AlbumDto = AlbumDto(
        albumId = albumId,
        title = title,
        description = description,
        //tripId = trip!!.tripId,
        media = media.map { it.toDto() }.toMutableSet()
    )

}
