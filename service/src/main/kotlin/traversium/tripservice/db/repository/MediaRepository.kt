package traversium.tripservice.db.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Media
import java.util.Optional

@Repository
interface MediaRepository : JpaRepository<Media, Long> {

    fun findByPathUrl(pathUrl: String): Optional<Media>

    @Query("""
        SELECT m 
        FROM Media m
        JOIN Album a ON m MEMBER OF a.media
        JOIN Trip t ON a MEMBER OF t.albums
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE t.ownerId = :userId 
           OR :userId MEMBER OF t.collaborators 
           OR :userId MEMBER OF t.viewers 
           OR t.visibility = 1 
    """)
    fun findAllAccessibleMediaByUserId(userId: String): List<Media>

    @Query("""
        SELECT m 
        FROM Media m
        JOIN Album a ON m MEMBER OF a.media
        JOIN Trip t ON a MEMBER OF t.albums
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE t.ownerId = :userId 
           OR :userId MEMBER OF t.collaborators 
           OR :userId MEMBER OF t.viewers 
           OR t.visibility = 1 
    """)
    fun findAllAccessibleMediaByUserId(@Param("userId") userId: String, pageable: Pageable): List<Media>

    @Query("""
        SELECT m 
        FROM Media m
        JOIN Album a ON m MEMBER OF a.media
        JOIN Trip t ON a MEMBER OF t.albums
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE m.uploader = :uploaderId 
          AND (t.ownerId = :userId 
           OR :userId MEMBER OF t.collaborators 
           OR :userId MEMBER OF t.viewers 
           OR t.visibility = 1)
    """)
    fun findAccessibleMediaByUploader(uploaderId: String, userId: String): List<Media>

    @Query("""
        SELECT m 
        FROM Media m
        JOIN Album a ON m MEMBER OF a.media
        JOIN Trip t ON a MEMBER OF t.albums
        LEFT JOIN t.collaborators c
        LEFT JOIN t.viewers v
        WHERE m.uploader = :uploaderId 
          AND (t.ownerId = :userId 
           OR :userId MEMBER OF t.collaborators 
           OR :userId MEMBER OF t.viewers 
           OR t.visibility = 1)
    """)
    fun findAccessibleMediaByUploader(@Param("uploaderId") uploaderId: String, @Param("userId") userId: String, pageable: Pageable): List<Media>
}