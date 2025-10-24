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

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinTable(
//        name = "trip_albums",
//        joinColumns = [JoinColumn(name = "trip_id")],
//        inverseJoinColumns = [JoinColumn(name = "album_id")],
//        indexes = [
//            Index(name = "idx_trip_albums_trip", columnList = "trip_id"),
//            Index(name = "idx_trip_albums_album", columnList = "album_id")
//        ]
//    )
//    var trip: Trip? = null,

    @Column(name="title", nullable = false)
    var title: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @OneToMany(mappedBy = "album", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var media: MutableSet<Media> = mutableSetOf()
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
