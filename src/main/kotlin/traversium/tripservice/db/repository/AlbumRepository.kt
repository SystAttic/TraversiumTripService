package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Album

@Repository
interface AlbumRepository : JpaRepository<Album, Long> {

    @Query("""
        SELECT a 
        FROM Album a 
        JOIN Trip t ON a MEMBER OF t.albums 
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v 
        WHERE t.ownerId = :userId OR c = :userId OR v = :userId OR t.visibility = 1
    """)
    fun findAllAccessibleAlbumsByUserId(userId: String): List<Album> // 💡 Fixed return type
}