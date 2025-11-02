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
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.AlbumWithoutMediaException
import traversium.tripservice.service.AlbumService

@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class AlbumServiceIntegrationTest @Autowired constructor(
    private val albumService: AlbumService
) {

    @Test
    fun `create an album`() {
        val dto = AlbumDto(
            albumId = null,
            title = "Test Album",
            description = "An album for testing",
            media = emptyList()
        )

        val created = albumService.createAlbum(dto)
        assertNotNull(created.albumId)
        assertEquals("Test Album", created.title)
    }

    @Test
    fun `get all albums`() {
        albumService.createAlbum(AlbumDto(null, "Album A", "desc", emptyList()))
        albumService.createAlbum(AlbumDto(null, "Album B", "desc", emptyList()))

        val allAlbums = albumService.getAllAlbums()
        assertTrue(allAlbums.size >= 2)
        assertTrue(allAlbums.any { it.title == "Album A" })
    }

    @Test
    fun `get album by ID`() {
        val created = albumService.createAlbum(AlbumDto(null, "My Album", "desc", emptyList()))
        val found = albumService.getByAlbumId(created.albumId!!)

        assertEquals(created.albumId, found.albumId)
        assertEquals("My Album", found.title)
    }

    @Test
    fun `update album`() {
        val created = albumService.createAlbum(AlbumDto(null, "Old Title", "desc", emptyList()))
        val updated = albumService.updateAlbum(created.albumId!!, AlbumDto(null, "New Title", "desc", emptyList()))

        assertEquals("New Title", updated.title)
    }

    @Test
    fun `delete album`() {
        val created = albumService.createAlbum(AlbumDto(null, "Album To Delete", "desc", emptyList()))
        val beforeDelete = albumService.getAllAlbums().size

        albumService.deleteAlbum(created.albumId!!)
        val afterDelete = albumService.getAllAlbums().size

        assertTrue(afterDelete < beforeDelete)
        assertThrows(AlbumNotFoundException::class.java) {
            albumService.getByAlbumId(created.albumId!!)
        }
    }

    @Test
    fun `add media to album`() {
        val album = albumService.createAlbum(AlbumDto(null, "Media Album", "desc", emptyList()))
        val dto = MediaDto(
            mediaId = null,
            pathUrl = "file1.jpg",
            ownerId = "user_1",
            fileType = "image",
            fileFormat = "jpg",
            fileSize = 100L
        )

        val updated = albumService.addMediaToAlbum(album.albumId!!, dto)
        assertEquals(1, updated.media.size)
        assertEquals("file1.jpg", updated.media.first().pathUrl)
    }

    @Test
    fun `delete media from album`() {
        val album = albumService.createAlbum(AlbumDto(null, "Album with Media", "desc", emptyList()))
        val mediaDto = MediaDto(null, "path.jpg", "owner", "image", "jpg", 10L)
        albumService.addMediaToAlbum(album.albumId!!, mediaDto)

        val updated = albumService.getByAlbumId(album.albumId!!)
        val mediaId = updated.media.first().mediaId!!

        albumService.deleteMediaFromAlbum(album.albumId!!, mediaId)
        val after = albumService.getByAlbumId(album.albumId!!)

        assertEquals(0, after.media.size)
    }

    @Test
    fun `get media from album`() {
        val album = albumService.createAlbum(AlbumDto(null, "Media Album", "desc", emptyList()))
        val dto = MediaDto(null, "media1.jpg", "owner_1", "image", "jpg", 10L)
        albumService.addMediaToAlbum(album.albumId!!, dto)

        val updated = albumService.getByAlbumId(album.albumId!!)
        val media = albumService.getMediaFromAlbum(album.albumId!!, updated.media.first().mediaId!!)

        assertEquals("media1.jpg", media.pathUrl)
    }

    @Test
    fun `get media from album without media should throw`() {
        val album = albumService.createAlbum(AlbumDto(null, "Empty Album", "desc", emptyList()))
        assertThrows(AlbumWithoutMediaException::class.java) {
            albumService.getMediaFromAlbum(album.albumId!!, 999L)
        }
    }

    @Test
    fun `delete album that does not exist should throw`() {
        assertThrows(AlbumNotFoundException::class.java) {
            albumService.deleteAlbum(999999L)
        }
    }

    @Test
    fun `update album that does not exist should throw`() {
        assertThrows(AlbumNotFoundException::class.java) {
            albumService.updateAlbum(999999L, AlbumDto(null, "NonExistent", "desc", emptyList()))
        }
    }
}
