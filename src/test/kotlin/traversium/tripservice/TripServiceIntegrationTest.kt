package traversium.tripservice

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.service.TripService

@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TripServiceIntegrationTest @Autowired constructor(
    private val tripService: TripService,
    private val tripRepository: TripRepository
) {

    @Test
    fun `create and update trip`() {
        val dto = TripDto(
            tripId = null,
            title = "My Test Trip",
            description = "Integration testing trip creation",
            ownerId = "user_1",
            coverPhotoUrl = null,
            collaborators = listOf("user_2", "user_3"),
            viewers = emptyList(),
            albums = mutableListOf()
        )

        val created = tripService.createTrip(dto)
        val createdId = created.tripId
        requireNotNull(createdId) { "Trip ID should not be null after creation" }

        assertEquals("My Test Trip", created.title)

        val updated = tripService.updateTrip(createdId, created.copy(title = "Updated Trip"))
        assertEquals("Updated Trip", updated.title)
    }

    @Test
    fun `add album to trip`() {
        val dto = TripDto(
            tripId = null,
            title = "Trip with Album",
            description = "Integration testing trip creation",
            ownerId = "user_1",
            coverPhotoUrl = null,
            collaborators = listOf("user_2", "user_3"),
            viewers = emptyList(),
            albums = mutableListOf()
        )
        val trip = tripService.createTrip(dto)
        val tripId = trip.tripId
        requireNotNull(tripId) { "Trip ID should not be null after creation" }

        val albumDto = AlbumDto(
            albumId = null,
            title = "My Album",
            description = "Trip photos",
            media = emptyList()
        )

        tripService.addAlbumToTrip(tripId, albumDto)
        val found = tripService.getByTripId(tripId)
        assertEquals(1, found.albums.size)
    }

    @Test
    fun `delete album from trip`() {
        val albumDto = AlbumDto(
            albumId = null,
            title = "My Album",
            description = "Trip photos",
            media = emptyList()
        )

        val tripDto = TripDto(
            tripId = null,
            title = "Trip with Album",
            description = "Integration testing trip creation",
            ownerId = "user_1",
            coverPhotoUrl = null,
            collaborators = listOf("user_2", "user_3"),
            viewers = emptyList(),
            albums = mutableListOf()
        )
        val trip = tripService.createTrip(tripDto)
        val tripId = trip.tripId
        requireNotNull(tripId) { "Trip ID should not be null after creation" }

        tripService.addAlbumToTrip(tripId, albumDto)
        var found = tripService.getByTripId(tripId)
        assertEquals(1, found.albums.size)

        val savedAlbumId = found.albums.first().albumId!!
        val album = tripService.getAlbumFromTrip(tripId, savedAlbumId)

        tripService.deleteAlbumFromTrip(tripId, album.albumId!!)

        found = tripService.getByTripId(tripId)
        assertEquals(0, found.albums.size)

    }
}
