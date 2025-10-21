package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.TripDto

@Entity
@Table(name = Trip.TABLE_NAME)
data class Trip(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tripId: Long = 0,

    @Column(nullable = false)
    val title: String,

    val description: String? = null,

    @Column(nullable = false)
    val owner: String, // Keycloak user ID

    @Column(name = "cover_photo_url")
    val coverPhotoUrl: String? = null,

    /* ---- Collaborators / Editors ---- */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "trip_collaborators",
        joinColumns = [JoinColumn(name = "trip_id")]
    )
    @Column(name = "collaborator_id")
    val collaborators: Set<String> = emptySet(),

    /* ---- Viewers ---- */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "trip_viewers",
        joinColumns = [JoinColumn(name = "trip_id")]
    )
    @Column(name = "viewer_id")
    val viewers: Set<String> = emptySet(),

    /* ---- Albums ---- */
    @OneToMany(mappedBy = "trip", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val albums: List<Album> = emptyList()
){
    companion object{
        const val TABLE_NAME = "trip"
    }

    fun toDto() = TripDto(tripId, title, description, owner, coverPhotoUrl, collaborators, viewers, albums)
}