package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Trip

@Repository
interface TripRepository : JpaRepository<Trip, Long> {

    // user gets all trips from his own profile
    @Query("""
        SELECT DISTINCT t 
        FROM Trip t 
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE
            t.ownerId = :userId OR 
            c = :userId OR 
            v = :userId
    """)
    fun findAllAccessibleTripsByUserId(userId: String): List<Trip>

    // user gets all owned trips
    @Query("""
        SELECT DISTINCT t 
        FROM Trip t 
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE 
            t.ownerId = :ownerId
    """)
    fun findByOwnerId(ownerId: String): List<Trip>

    // user gets all trips not owned by user
    @Query("""
        SELECT DISTINCT t 
        FROM Trip t 
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE 
            t.ownerId = :ownerId AND 
            (
                t.visibility = 1 OR 
                c = :userId OR 
                v = :userId
            )
    """)
    fun findByOwnerId(ownerId: String, userId: String): List<Trip>

    @Query("""
        SELECT DISTINCT t 
        FROM Trip t 
        LEFT JOIN t.collaborators c
        WHERE 
            t.ownerId = :userId OR 
            c = :userId
    """)
    fun findByOwnerOrCollaborator(userId: String): List<Trip>

    // user gets all trips, where the user is their collaborator
    @Query("select t from Trip t join t.collaborators e where e = :collaboratorId")
    fun findByCollaboratorId(collaboratorId: String): List<Trip>

    // user gets all trips, where the other user is a collaborator and my user can see those trips
    @Query("""
        SELECT DISTINCT t 
        FROM Trip t 
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v 
            where c = :collaboratorId AND 
            (
                t.visibility = 1 OR
                t.ownerId = :userId OR
                v = :userId
            )
    """)
    fun findByCollaboratorId(collaboratorId: String, userId: String): List<Trip>

    // user gets all trips who are viewed by the user
    @Query("select t from Trip t join t.viewers e where e = :viewerId")
    fun findByViewerId(viewerId: String): List<Trip>

    @Query("""
        SELECT DISTINCT t 
        FROM Trip t 
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE 
            t.ownerId = :ownerId AND 
            (
                c = :blockedId OR 
                v = :blockedId
            )
    """)
    fun findOwnedForBlocking(ownerId: String, blockedId: String): List<Trip>
}
