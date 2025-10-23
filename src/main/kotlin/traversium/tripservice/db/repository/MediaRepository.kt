package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Media

@Repository
interface MediaRepository : JpaRepository<Media, Long> {
    @Query("SELECT m FROM Media m WHERE m.album.albumId = :albumId")
    fun findByAlbumId(@Param("albumId") albumId: Long): List<Media>

    @Query("SELECT m FROM Media m WHERE m.mediaId = :mediaId AND m.album.albumId = :albumId")
    fun findByMediaIdAndAlbumId(
        @Param("mediaId") mediaId: Long,
        @Param("albumId") albumId: Long
    ): Media?
}