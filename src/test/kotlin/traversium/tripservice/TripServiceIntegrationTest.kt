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
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.*
import traversium.tripservice.service.TripService

@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TripServiceIntegrationTest @Autowired constructor(
    private var tripService: TripService,
) {

    @Test
    fun `create a trip`(){
        val dto = TripDto(
            tripId = null,
            title = "New Trip",
            description = "Testing creation",
            ownerId = "owner_1",
            coverPhotoUrl = "url",
            collaborators = listOf("user_2", "user_3"),
            viewers = emptyList(),
            albums = mutableListOf()
        )

        val created = tripService.createTrip(dto)
        assertNotNull(created.tripId)
        assertEquals("New Trip", created.title)
        assertEquals("owner_1", created.ownerId)
        assertEquals(2, created.collaborators.size)
    }

    @Test
    fun `get all Trips`(){
        val trip1 = tripService.createTrip(
            TripDto(null, "Trip A", "Description", "user_1", null, emptyList(), emptyList(), mutableListOf())
        )
        val trip2 = tripService.createTrip(
            TripDto(null, "Trip B", "Description", "user_2", null, emptyList(), emptyList(), mutableListOf())
        )

        val allTrips = tripService.getAllTrips()
        assertTrue(allTrips.size >= 2)
        assertTrue(allTrips.any { it.title == trip1.title })
        assertTrue(allTrips.any { it.title == trip2.title })

    }

    @Test
    fun `get Trip by tripId`() {
        val created = tripService.createTrip(
            TripDto(null, "Trip X", "Desc", "user_x", null, emptyList(), emptyList(), mutableListOf())
        )
        val found = tripService.getByTripId(created.tripId!!)
        assertEquals(created.tripId, found.tripId)
        assertEquals("Trip X", found.title)
    }

    @Test
    fun `get all Trips by ownerId`(){
        val ownerId = "user_owner"
        tripService.createTrip(TripDto(null, "Owner Trip 1", "desc", ownerId, null, emptyList(), emptyList(), mutableListOf()))
        tripService.createTrip(TripDto(null, "Owner Trip 2", "desc", ownerId, null, emptyList(), emptyList(), mutableListOf()))
        tripService.createTrip(TripDto(null, "Other Trip", "desc", "another", null, emptyList(), emptyList(), mutableListOf()))

        val found = tripService.getTripsByOwner(ownerId)
        assertEquals(2, found.size)
        assertTrue(found.all { it.ownerId == ownerId })
    }

    @Test
    fun `update a trip`() {
        val dto = TripDto(
            tripId = null,
            title = "My Test Trip",
            description = "Integration testing trip update",
            ownerId = "user_1",
            coverPhotoUrl = null,
            collaborators = listOf("user_2", "user_3"),
            viewers = emptyList(),
            albums = mutableListOf()
        )

        val created = tripService.createTrip(dto)
        val createdId = created.tripId
        requireNotNull(createdId) { "Trip ID should not be null after creation" }

        val updated = tripService.updateTrip(createdId, created.copy(title = "Updated Trip"))
        assertEquals("Updated Trip", updated.title)
    }

    @Test
    fun `delete a Trip`() {
        val created = tripService.createTrip(
            TripDto(null, "Trip to Delete", "desc", "user_del", null, emptyList(), emptyList(), mutableListOf())
        )

        val tripId = created.tripId!!
        val beforeDelete = tripService.getAllTrips().size

        tripService.deleteTrip(tripId)
        val afterDelete = tripService.getAllTrips().size

        assertTrue(afterDelete < beforeDelete)

        assertThrows(traversium.tripservice.exceptions.TripNotFoundException::class.java) {
            tripService.getByTripId(tripId)
        }
    }

    @Test
    fun `get all Trips by collaboratorId`() {
        val collaborator = "collab_user"
        tripService.createTrip(TripDto(null, "Collab Trip 1", "desc", "owner_1", null, listOf(collaborator), emptyList(), mutableListOf()))
        tripService.createTrip(TripDto(null, "Collab Trip 2", "desc", "owner_2", null, listOf(collaborator), emptyList(), mutableListOf()))
        tripService.createTrip(TripDto(null, "Non Collab Trip", "desc", "owner_3", null, listOf("other"), emptyList(), mutableListOf()))

        // convert to DTOs if service doesn’t already return them detached
        val found = tripService.getTripsByCollaborator(collaborator).map { it }

        assertTrue(found.size >= 2)
        found.forEach { trip ->
            assertTrue(trip.collaborators.contains(collaborator))
        }
    }

    @Test
    fun `add a Collaborator to Trip`() {
        val trip = tripService.createTrip(
            TripDto(null, "Trip Collab Add", "desc", "owner_1", null, listOf("user_2"), emptyList(), mutableListOf())
        )
        val updated = tripService.addCollaboratorToTrip(trip.tripId!!, "new_user")

        assertTrue(updated.collaborators.contains("new_user"))
        assertEquals(2, updated.collaborators.size)
    }

    @Test
    fun `delete a Collaborator from Trip`() {
        val trip = tripService.createTrip(
            TripDto(null, "Trip Collab Del", "desc", "owner_1", null, listOf("user_a", "user_b"), emptyList(), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        tripService.deleteCollaboratorFromTrip(tripId, "user_a")

        val found = tripService.getByTripId(tripId)
        assertFalse(found.collaborators.contains("user_a"))
        assertEquals(1, found.collaborators.size)
    }

    @Test
    fun `add a Viewer to Trip`() {
        val trip = tripService.createTrip(
            TripDto(null, "Trip View Add", "desc", "owner_1", null, emptyList(), listOf(), mutableListOf())
        )

        val updated = tripService.addViewerToTrip(trip.tripId!!, "viewer_1")
        assertTrue(updated.viewers.contains("viewer_1"))
    }

    @Test
    fun `delete a Viewer from Trip`() {
        val trip = tripService.createTrip(
            TripDto(null, "Trip Viewer deletion", "desc", "owner_1", null, emptyList(), listOf("viewer_a", "viewer_b"), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        tripService.deleteViewerFromTrip(tripId, "viewer_b")

        val found = tripService.getByTripId(tripId)
        assertFalse(found.viewers.contains("viewer_b"))
        assertEquals(1, found.viewers.size)
    }

    @Test
    fun `get an Album from Trip`() {
        val trip = tripService.createTrip(
            TripDto(null, "Trip With Album", "desc", "owner_1", null, emptyList(), emptyList(), mutableListOf())
        )
        val albumDto = AlbumDto(null, "Album 1", "desc", emptyList())
        val tripId = requireNotNull(trip.tripId)

        tripService.addAlbumToTrip(tripId, albumDto)
        val foundTrip = tripService.getByTripId(tripId)

        val foundAlbum = tripService.getAlbumFromTrip(tripId, foundTrip.albums.first().albumId!!)
        assertEquals("Album 1", foundAlbum.title)
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

    // <--- Edge case tests --->
    @Test
    fun `delete album from trip without albums should throw`() {
        val trip = tripService.createTrip(
            TripDto(null, "Empty Album Trip", "desc", "owner_edge", null, emptyList(), emptyList(), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        assertThrows(AlbumNotFoundException::class.java) {
            tripService.deleteAlbumFromTrip(tripId, 9999L)
        }
    }

    @Test
    fun `get album from trip without albums should throw`() {
        val trip = tripService.createTrip(
            TripDto(null, "No Album Trip", "desc", "owner_x", null, emptyList(), emptyList(), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        assertThrows(TripWithoutAlbumsException::class.java) {
            tripService.getAlbumFromTrip(tripId, 123L)
        }
    }

    @Test
    fun `add collaborator that already exists should throw`() {
        val trip = tripService.createTrip(
            TripDto(null, "Duplicate Collaborator Trip", "desc", "owner_1", null, listOf("user_a"), emptyList(), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        assertThrows(TripHasCollaboratorException::class.java) {
            tripService.addCollaboratorToTrip(tripId, "user_a")
        }
    }

    @Test
    fun `delete collaborator that does not exist should throw`() {
        val trip = tripService.createTrip(
            TripDto(null, "No Such Collaborator Trip", "desc", "owner_1", null, listOf("user_b"), emptyList(), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        assertThrows(TripWithoutCollaboratorException::class.java) {
            tripService.deleteCollaboratorFromTrip(tripId, "user_not_here")
        }
    }

    @Test
    fun `add viewer that already exists should throw`() {
        val trip = tripService.createTrip(
            TripDto(null, "Duplicate Viewer Trip", "desc", "owner_1", null, emptyList(), listOf("viewer_1"), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        assertThrows(TripHasViewerException::class.java) {
            tripService.addViewerToTrip(tripId, "viewer_1")
        }
    }

    @Test
    fun `delete viewer that does not exist should throw`() {
        val trip = tripService.createTrip(
            TripDto(null, "Viewer Not Found Trip", "desc", "owner_1", null, emptyList(), listOf("viewer_2"), mutableListOf())
        )

        val tripId = requireNotNull(trip.tripId)
        assertThrows(TripWithoutViewerException::class.java) {
            tripService.deleteViewerFromTrip(tripId, "viewer_999")
        }
    }

    @Test
    fun `get trip that does not exist should throw`() {
        assertThrows(traversium.tripservice.exceptions.TripNotFoundException::class.java) {
            tripService.getByTripId(999999L)
        }
    }

    @Test
    fun `delete trip that does not exist should throw`() {
        assertThrows(traversium.tripservice.exceptions.TripNotFoundException::class.java) {
            tripService.deleteTrip(999999L)
        }
    }

    @Test
    fun `add album to non existing trip should throw`() {
        val albumDto = AlbumDto(null, "Ghost Album", "desc", emptyList())

        assertThrows(traversium.tripservice.exceptions.TripNotFoundException::class.java) {
            tripService.addAlbumToTrip(123456L, albumDto)
        }
    }

    @Test
    fun `get trips for collaborator that has none should return empty`() {
        val user = "user_lonely"
        val found = tripService.getTripsByCollaborator(user)
        assertTrue(found.isEmpty(), "Expected no trips for collaborator $user")
    }

    @Test
    fun `get trips for owner that has none should return empty`() {
        val user = "owner_lonely"
        val found = tripService.getTripsByOwner(user)
        assertTrue(found.isEmpty(), "Expected no trips for owner $user")
    }


}
