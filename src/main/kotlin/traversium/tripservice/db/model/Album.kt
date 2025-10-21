package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.AlbumDto

@Entity
@Table(name = Album.TABLE_NAME)
data class Album(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val albumId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    val trip: Trip? = null,

    @Column(nullable = false)
    val title: String,

    val description: String? = null,

    @OneToMany(mappedBy = "album", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val media: List<Media> = emptyList()
) {
    companion object {
        const val TABLE_NAME = "album"
    }

    fun toDto(): AlbumDto = AlbumDto(
        albumId = albumId,
        title = title,
        description = description,
        tripId = trip!!.tripId,
        media = media.map { it.toDto() }
    )

}
