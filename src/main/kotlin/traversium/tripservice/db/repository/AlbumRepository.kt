package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Album
import traversium.tripservice.db.model.Media

@Repository
interface AlbumRepository : JpaRepository<Album, Long> {

    @Query("select a.media from Album a where a.albumId = :albumId")
    fun getMedia(@Param("albumId") albumId: Long): List<Media>

    fun getByTripId(@Param("tripId") tripId: Long): List<Album>
}
