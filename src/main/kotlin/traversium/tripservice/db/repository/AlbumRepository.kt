package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Album

@Repository
interface AlbumRepository : JpaRepository<Album, Long> {
    fun findByTripId(tripId: Long): List<Album>
}
