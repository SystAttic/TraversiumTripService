package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.TripDto

@Entity
@Table(name = Trip.TABLE_NAME)
data class Trip(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id", unique = true, nullable = false, updatable = false, length = 36)
    var tripId: Long? = null,

    @Column(nullable = false)
    val title: String,

    val description: String? = null,

    @Column(nullable = false)
    val owner: String, // Keycloak user ID

    @Column(name = "cover_photo_url")
    val coverPhotoUrl: String? = null,

    /* ---- Collaborators / Editors ---- */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "trip_collaborators",
        joinColumns = [JoinColumn(name = "trip_id")],
        indexes = [
            Index(name = "idx_trip_collaborators_trip", columnList = "trip_id"),
            Index(name = "idx_trip_collaborators_user", columnList = "collaborator_id")
        ]
    )
    @Column(name = "collaborator_id")
    val collaborators: Set<String> = emptySet(),

    /* ---- Viewers ---- */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "trip_viewers",
        joinColumns = [JoinColumn(name = "trip_id")]
    )
    @Column(name = "viewer_id")
    val viewers: Set<String> = emptySet(),

    /* ---- Albums ---- */
    @OneToMany(mappedBy = "trip", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val albums: MutableSet<Album> = mutableSetOf(),
){
    companion object{
        const val TABLE_NAME = "trip"
    }

    fun toDto() = TripDto(tripId, title, description, owner, coverPhotoUrl, collaborators, viewers, albums.map{it.toDto()}.toMutableSet())
}