package traversium.tripservice

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import traversium.tripservice.db.model.Media
import traversium.tripservice.db.repository.MediaRepository
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.exceptions.MediaUnauthorizedException
import traversium.tripservice.security.BaseSecuritySetup
import traversium.tripservice.service.FirebaseService
import traversium.tripservice.service.MediaService
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class MediaServiceTest : BaseSecuritySetup() {

    @Mock private lateinit var mediaRepository: MediaRepository
    @Mock private lateinit var firebaseService: FirebaseService
    @Mock private lateinit var tripRepository: TripRepository

    @InjectMocks
    private lateinit var mediaService: MediaService

    private val MEDIA_ID = 100L
    private val OTHER_MEDIA_ID = 101L
    private val TRIP_ID = 1L
    private val OWNER_ID = firebaseId // Authenticated user
    private val OTHER_UPLOADER_ID = "uploader_b"

    private val defaultMedia = Media(
        mediaId = MEDIA_ID,
        pathUrl = "test_media.jpg",
        uploader = OWNER_ID,
        fileType = "image",
        fileFormat = "jpg",
        fileSize = 100L,
        createdAt = OffsetDateTime.now()
    )

    // --- Setup ---
    @BeforeEach
    fun setUp() {
        setupDefaultFirebaseMocks()
    }

    private fun setupDefaultFirebaseMocks() {
        lenient().`when`(firebaseService.extractUidFromToken(token)).thenReturn(OWNER_ID)
    }


    @Test
    fun `getAllMedia success returns list of accessible media`() {
        val accessibleMedia = listOf(defaultMedia, defaultMedia.copy(mediaId = OTHER_MEDIA_ID))

        `when`(mediaRepository.findAllAccessibleMediaByUserId(OWNER_ID)).thenReturn(accessibleMedia)

        val result = mediaService.getAllMedia()

        assertEquals(2, result.size)
        verify(mediaRepository).findAllAccessibleMediaByUserId(OWNER_ID)
    }

    @Test
    fun `getAllMedia throws not found if list is empty`() {
        `when`(mediaRepository.findAllAccessibleMediaByUserId(OWNER_ID)).thenReturn(emptyList())

        assertThrows(MediaNotFoundException::class.java) {
            mediaService.getAllMedia()
        }
    }

    @Test
    fun `getMediaById success when user is authorized to view trip`() {
        `when`(tripRepository.findTripIdByMediaId(MEDIA_ID)).thenReturn(Optional.of(TRIP_ID))
        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)
        `when`(mediaRepository.findById(MEDIA_ID)).thenReturn(Optional.of(defaultMedia))

        val found = mediaService.getMediaById(MEDIA_ID)

        assertEquals("test_media.jpg", found.pathUrl)
        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `getMediaById throws unauthorized if user is not authorized to view trip`() {
        `when`(tripRepository.findTripIdByMediaId(MEDIA_ID)).thenReturn(Optional.of(TRIP_ID))
        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(false)

        assertThrows(MediaUnauthorizedException::class.java) {
            mediaService.getMediaById(MEDIA_ID)
        }
        verify(mediaRepository, never()).findById(any())
    }

    @Test
    fun `getMediaById throws not found if media is not linked to a trip`() {
        `when`(tripRepository.findTripIdByMediaId(MEDIA_ID)).thenReturn(Optional.empty())

        assertThrows(MediaNotFoundException::class.java) {
            mediaService.getMediaById(MEDIA_ID)
        }
        verify(tripRepository, never()).isUserAuthorizedToView(anyLong(), anyString())
    }

    @Test
    fun `getMediaById throws not found if media exists but not found by repository`() {
        `when`(tripRepository.findTripIdByMediaId(MEDIA_ID)).thenReturn(Optional.of(TRIP_ID))
        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)
        `when`(mediaRepository.findById(MEDIA_ID)).thenReturn(Optional.empty())

        assertThrows(MediaNotFoundException::class.java) {
            mediaService.getMediaById(MEDIA_ID)
        }
    }

    @Test
    fun `getMediaByUploader success returns accessible media`() {
        val uploaderId = OTHER_UPLOADER_ID
        val accessibleMedia = listOf(
            defaultMedia.copy(uploader = uploaderId, mediaId = 200L)
        )

        `when`(mediaRepository.findAccessibleMediaByUploader(uploaderId, OWNER_ID)).thenReturn(accessibleMedia)

        val result = mediaService.getMediaByUploader(uploaderId)

        assertEquals(1, result.size)
        assertEquals(uploaderId, result.first().uploader)
        verify(mediaRepository).findAccessibleMediaByUploader(uploaderId, OWNER_ID)
    }

    @Test
    fun `getMediaByUploader throws not found if list is empty`() {
        val uploaderId = OTHER_UPLOADER_ID
        `when`(mediaRepository.findAccessibleMediaByUploader(uploaderId, OWNER_ID)).thenReturn(emptyList())

        assertThrows(MediaNotFoundException::class.java) {
            mediaService.getMediaByUploader(uploaderId)
        }
    }
}