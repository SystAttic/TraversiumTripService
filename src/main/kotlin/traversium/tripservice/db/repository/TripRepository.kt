package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Trip
import java.util.Optional

@Repository
interface TripRepository : JpaRepository<Trip, Long> {

    fun findByOwnerId(ownerId: String): List<Trip>

    @Query("select t from Trip t join t.collaborators e where e = :collaboratorId")
    fun findByCollaboratorId(collaboratorId: String): List<Trip>

    @Query("select t from Trip t join t.viewers e where e = :viewerId")
    fun findByViewerId(viewerId: String): List<Trip>
}
