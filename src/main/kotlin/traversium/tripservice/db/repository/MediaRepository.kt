package traversium.tripservice.db.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import traversium.tripservice.db.model.Media

@Repository
interface MediaRepository : JpaRepository<Media, Long> {

}