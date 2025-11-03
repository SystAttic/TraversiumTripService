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
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.service.MediaService
import traversium.tripservice.db.repository.MediaRepository
import traversium.tripservice.db.model.Media

@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class MediaServiceIntegrationTest @Autowired constructor(
    private val mediaService: MediaService,
    private val mediaRepository: MediaRepository
) {

    @Test
    fun `get all media`() {
        mediaRepository.save(Media(null, "a.jpg", "user1", "image", "jpg", 100L))
        mediaRepository.save(Media(null, "b.jpg", "user2", "image", "jpg", 200L))

        val all = mediaService.getAllMedia()
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.pathUrl == "a.jpg" })
    }

    @Test
    fun `get media by ID`() {
        val saved = mediaRepository.save(Media(null, "photo.jpg", "owner1", "image", "jpg", 50L))
        val found = mediaService.getMediaById(saved.mediaId!!)

        assertEquals("photo.jpg", found.pathUrl)
    }

    @Test
    fun `get media by owner`() {
        mediaRepository.save(Media(null, "img1.jpg", "ownerA", "image", "jpg", 20L))
        mediaRepository.save(Media(null, "img2.jpg", "ownerA", "image", "jpg", 30L))
        mediaRepository.save(Media(null, "img3.jpg", "ownerB", "image", "jpg", 40L))

        val ownerMedia = mediaService.getMediaByOwner("ownerA")

        assertEquals(2, ownerMedia.size)
        assertTrue(ownerMedia.all { it.ownerId == "ownerA" })
    }

    @Test
    fun `get media by non-existing ID should throw`() {
        assertThrows(MediaNotFoundException::class.java) {
            mediaService.getMediaById(999999L)
        }
    }

    @Test
    fun `get media by owner that has none should return empty`() {
        val empty = mediaService.getMediaByOwner("no_user")
        assertTrue(empty.isEmpty())
    }
}
