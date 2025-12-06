package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.db.model.Media
import traversium.tripservice.dto.TripDto
import java.time.OffsetDateTime

@Entity
@Table(name = Trip.TABLE_NAME)
data class Trip(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id", unique = true, nullable = false, updatable = false, length = 36)
    var tripId: Long? = null,

    @Column(name="title", nullable = false)
    val title: String? = null,

    @Column(name="description")
    val description: String? = null,

    @Column(name="owner_id", nullable = false)
    val ownerId: String? = null, // Firebase user ID

    @Column(name="visibility", nullable = false)
    val visibility: Visibility? = null,

    @Column(name = "cover_photo_url")
    val coverPhotoUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,

    /* ---- Collaborators / Editors ---- */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "trip_collaborators",
        joinColumns = [JoinColumn(name = "trip_id")],
        indexes = [
            Index(name = "idx_trip_collaborators_trip", columnList = "trip_id"),
            Index(name = "idx_trip_collaborators_user", columnList = "collaborator_id")
        ]
    )
    @Column(name = "collaborator_id")
    val collaborators: MutableList<String> = mutableListOf(),

    /* ---- Viewers ---- */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "trip_viewers",
        joinColumns = [JoinColumn(name = "trip_id")]
    )
    @Column(name = "viewer_id")
    val viewers: MutableList<String> = mutableListOf(),

    /* ---- Albums ---- */
    @OneToMany(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
        name = "trip_albums",
        joinColumns = [JoinColumn(name = "trip_id")],
        inverseJoinColumns = [JoinColumn(name = "album_id")],
        indexes = [
            Index(name = "idx_trip_albums_trip", columnList = "trip_id"),
            Index(name = "idx_trip_albums_album", columnList = "album_id")
        ]
    )
    var albums: MutableList<Album> = mutableListOf(),
){
    companion object{
        const val TABLE_NAME = "trip"
    }
    @PrePersist
    protected fun onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now() // Only set for new entity
        }
    }

    fun toDto() : TripDto= TripDto(
        tripId,
        title,
        description,
        ownerId,
        visibility,
        coverPhotoUrl,
        collaborators,
        viewers,
        albums.map{it.toDto()}.toMutableList(),
        createdAt = createdAt
    )
}