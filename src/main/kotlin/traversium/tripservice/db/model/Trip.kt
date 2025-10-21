package traversium.tripservice.db.model

import jakarta.persistence.*
import traversium.tripservice.dto.TripDto

@Entity
@Table(name = Trip.TABLE_NAME)
data class Trip(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val tripId: Long = 0,

    @Column(name = "name")
    val name: String = "collection name",

    @Column(name = "cover_photo")
    val coverPhoto: String? = null,

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "owner_id", nullable = false)
    val owner: String,

    //@ManyToMany
    /*@JoinTable(
        name = "collection_editors",
        joinColumns = [JoinColumn(name = "collection_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )*/

    // A se to dela tako?
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_table",
        joinColumns = [JoinColumn(name = "uid")],
    )
    @Column(name = "editors")
    val editors: Set<String>? = emptySet(),

    //@ManyToMany
    /*@JoinTable(
        name = "collection_viewers",
        joinColumns = [JoinColumn(name = "collection_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )*/
    // TODO
    val viewers: Set<String>? = emptySet(),

    //@OneToMany(fetch = FetchType.LAZY)
    //@JoinColumn(name = "start_node_id")

    // TODO
    val albums: List<Long>? = null

){
    companion object{
        const val TABLE_NAME = "trip"
    }

    fun toDto() = TripDto(tripId, name, coverPhoto, owner, editors, viewers, albums)
}