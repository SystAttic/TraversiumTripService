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
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.AlbumWithoutMediaException
import traversium.tripservice.service.AlbumService
import traversium.tripservice.service.TripService
import java.time.OffsetDateTime
import java.time.ZoneOffset

@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class AlbumServiceIntegrationTest @Autowired constructor(
    private val albumService: AlbumService,
    @Autowired private val tripService: TripService
) {

    private fun createTripWithAlbum(tripTitle: String = "Trip with Album", albumTitle: String = "My Album"): Pair<Long, Long> {
        val trip = tripService.createTrip(
            TripDto(
                tripId = null,
                title = tripTitle,
                description = "desc",
                ownerId = "owner_1",
                visibility = Visibility.PRIVATE,
                coverPhotoUrl = null,
                collaborators = emptyList(),
                viewers = emptyList(),
                albums = mutableListOf()
            )
        )
        val albumDto = AlbumDto(null, albumTitle, "desc", emptyList())
        tripService.addAlbumToTrip(trip.tripId!!, albumDto)
        val foundTrip = tripService.getByTripId(trip.tripId!!)
        val albumId = foundTrip.albums.first().albumId!!
        return Pair(trip.tripId!!, albumId)
    }

    @Test
    fun `get all albums`() {
        val (_, album1) = createTripWithAlbum("Trip A", "Album A")
        val (_, album2) = createTripWithAlbum("Trip B", "Album B")

        val allAlbums = albumService.getAllAlbums()
        assertTrue(allAlbums.size >= 2)
        assertTrue(allAlbums.any { it.albumId == album1 })
        assertTrue(allAlbums.any { it.albumId == album2 })
    }

    @Test
    fun `get album by ID`() {
        val (_, albumId) = createTripWithAlbum("Trip X", "My Album")
        val found = albumService.getByAlbumId(albumId)
        assertEquals("My Album", found.title)
    }

    @Test
    fun `update album`() {
        val (_, albumId) = createTripWithAlbum("Trip Y", "Old Title")
        val updated = albumService.updateAlbum(albumId, AlbumDto(null, "New Title", "desc", emptyList()))
        assertEquals("New Title", updated.title)
    }

    @Test
    fun `add media to album`() {
        val (_, albumId) = createTripWithAlbum("Trip Z", "Media Album")
        val dto = MediaDto(
            mediaId = null,
            pathUrl = "file1.jpg",
            ownerId = "user_1",
            fileType = "image",
            fileFormat = "jpg",
            fileSize = 100L,
            createdAt = OffsetDateTime.now(),
        )

        val updated = albumService.addMediaToAlbum(albumId, dto)
        assertEquals(1, updated.media.size)
        assertEquals("file1.jpg", updated.media.first().pathUrl)
    }

    @Test
    fun `delete media from album`() {
        val (_, albumId) = createTripWithAlbum("Trip MediaDel", "Album with Media")
        val mediaDto = MediaDto(null, "path.jpg", "owner", "image", "jpg", fileSize = 10L, createdAt = OffsetDateTime.now(ZoneOffset.UTC))
        albumService.addMediaToAlbum(albumId, mediaDto)

        val updated = albumService.getByAlbumId(albumId)
        val mediaId = updated.media.first().mediaId!!

        albumService.deleteMediaFromAlbum(albumId, mediaId)
        val after = albumService.getByAlbumId(albumId)
        assertEquals(0, after.media.size)
    }

    @Test
    fun `get media from album`() {
        val (_, albumId) = createTripWithAlbum("Trip GetMedia", "Album Media")
        val dto = MediaDto(null, "media1.jpg", "owner_1", "image", "jpg", 10L, "",OffsetDateTime.now(ZoneOffset.UTC))
        albumService.addMediaToAlbum(albumId, dto)

        val updated = albumService.getByAlbumId(albumId)
        val media = albumService.getMediaFromAlbum(albumId, updated.media.first().mediaId!!)
        assertEquals("media1.jpg", media.pathUrl)
    }

    @Test
    fun `get media from album without media should throw`() {
        val (_, albumId) = createTripWithAlbum("Trip Empty", "Empty Album")
        assertThrows(AlbumWithoutMediaException::class.java) {
            albumService.getMediaFromAlbum(albumId, 999L)
        }
    }

    @Test
    fun `update album that does not exist should throw`() {
        assertThrows(AlbumNotFoundException::class.java) {
            albumService.updateAlbum(999999L, AlbumDto(null, "NonExistent", "desc", emptyList()))
        }
    }

    @Test
    fun `get album that does not exist should throw`() {
        assertThrows(AlbumNotFoundException::class.java) {
            albumService.getByAlbumId(999999L)
        }
    }
}
