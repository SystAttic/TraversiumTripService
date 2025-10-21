package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Media

@Repository
interface MediaRepository : JpaRepository<Media, Long> {
    fun findByAlbumId(albumId: Long): List<Media>
}
