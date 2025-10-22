package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Trip

@Repository
interface TripRepository : JpaRepository<Trip, Long> {
    fun findByOwner(owner: String): List<Trip>

    @Query("""
        select t
        from Trip t
        join t.collaborators e
        where e = :collaborator
        """
    )

    fun findByCollaborator(collaborator: String): List<Trip>
}
