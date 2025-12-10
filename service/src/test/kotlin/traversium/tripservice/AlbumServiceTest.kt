package traversium.tripservice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.ApplicationEventPublisher
import traversium.tripservice.db.model.Album
import traversium.tripservice.db.model.Media
import traversium.tripservice.db.model.Trip
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.AlbumUnauthorizedException
import traversium.tripservice.exceptions.AlbumWithoutMediaException
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.security.BaseSecuritySetup
import traversium.tripservice.service.AlbumService
import traversium.tripservice.service.FirebaseService
import traversium.tripservice.service.TripService
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AlbumServiceTest : BaseSecuritySetup() {

    @Mock private lateinit var albumRepository: AlbumRepository
    @Mock private lateinit var tripRepository: TripRepository
    @Mock private lateinit var eventPublisher: ApplicationEventPublisher
    @Mock private lateinit var firebaseService: FirebaseService
    @Mock private lateinit var tripService: TripService

    @InjectMocks
    private lateinit var albumService: AlbumService

    private val TRIP_ID = 1L
    private val ALBUM_ID = 99L
    private val MEDIA_ID = 500L
    private val OWNER_ID = firebaseId // Authenticated user for owner/collaborator tests
    private val OTHER_USER_ID = "firebase_other"
    private val COLLABORATOR_ID = "firebase_collab"
    private val VIEWER_ID = "firebase_viewer"
    private val UNAUTHORIZED_ID = "firebase_blocked"

    private val defaultMedia = Media(
        mediaId = MEDIA_ID,
        pathUrl = "file1.jpg",
        uploader = OTHER_USER_ID,
        fileType = "image",
        fileFormat = "jpg",
        fileSize = 100L,
        createdAt = OffsetDateTime.now()
    )

    private val defaultAlbum = Album(
        albumId = ALBUM_ID,
        title = "Test Album",
        description = "desc",
        media = mutableListOf(defaultMedia)
    )

    private fun getTestTrip(ownerId: String, visibility: Visibility, collaborators: List<String>, viewers: List<String>): Trip {
        return Trip(
            tripId = TRIP_ID,
            title = "Test Trip",
            ownerId = ownerId,
            visibility = visibility,
            collaborators = collaborators.toMutableList(),
            viewers = viewers.toMutableList(),
            albums = mutableListOf(defaultAlbum)
        )
    }

    @BeforeEach
    fun setUp() {
        lenient().`when`(firebaseService.extractUidFromToken(token)).thenReturn(OWNER_ID)
    }

    private fun mockAlbumToTripLink(albumId: Long = ALBUM_ID, tripId: Long = TRIP_ID) {
        `when`(tripRepository.findTripIdByAlbumId(albumId)).thenReturn(Optional.of(tripId))
    }

    private fun mockAuthorization(trip: Trip, isAuthorized: Boolean, isModify: Boolean) {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))

        val authenticatedId = OWNER_ID

        val expectedViewAuth = trip.visibility == Visibility.PUBLIC ||
                trip.collaborators.contains(authenticatedId) ||
                trip.viewers.contains(authenticatedId) ||
                trip.ownerId == authenticatedId

        val expectedModifyAuth = trip.collaborators.contains(authenticatedId) || trip.ownerId == authenticatedId

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
    }

    // Tests
    @Test
    fun `getAllAlbums success`() {
        val accessibleAlbums = listOf(
            defaultAlbum.copy(albumId = 10L, title = "Album A"),
            defaultAlbum.copy(albumId = 11L, title = "Album B")
        )
        `when`(albumRepository.findAllAccessibleAlbumsByUserId(OWNER_ID)).thenReturn(accessibleAlbums)

        val result = albumService.getAllAlbums()

        assertEquals(2, result.size)
        assertEquals("Album A", result[0].title)
        verify(albumRepository).findAllAccessibleAlbumsByUserId(OWNER_ID)
    }

    @Test
    fun `getAllAlbums throws not found if list is empty`() {
        `when`(albumRepository.findAllAccessibleAlbumsByUserId(OWNER_ID)).thenReturn(emptyList())

        assertThrows(AlbumNotFoundException::class.java) {
            albumService.getAllAlbums()
        }
    }

    @Test
    fun `getByAlbumId success for owner`() {
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(defaultAlbum))

        val found = albumService.getByAlbumId(ALBUM_ID)
        assertEquals("Test Album", found.title)
        verify(tripRepository).findById(TRIP_ID) // Verifies authorization check was performed
    }

    @Test
    fun `getByAlbumId success for collaborator`() {
        val trip = getTestTrip(
            ownerId = COLLABORATOR_ID,
            visibility = Visibility.PRIVATE,
            collaborators = listOf(OWNER_ID),
            viewers = emptyList()
        )
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(defaultAlbum))

        albumService.getByAlbumId(ALBUM_ID)
    }

    @Test
    fun `getByAlbumId unauthorized if private and not involved`() {
        val trip = getTestTrip(ownerId = OTHER_USER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))

        assertThrows(AlbumUnauthorizedException::class.java) {
            albumService.getByAlbumId(ALBUM_ID)
        }
    }

    @Test
    fun `getByAlbumId throws not found`() {
        mockAlbumToTripLink() // Link resolves fine
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.empty()) // Album not found

        assertThrows(AlbumNotFoundException::class.java) {
            albumService.getByAlbumId(ALBUM_ID)
        }
    }

    @Test
    fun `updateAlbum success for owner`() {
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(defaultAlbum))
        `when`(albumRepository.save(any<Album>())).thenAnswer { it.arguments[0] }
        `when`(tripService.getByTripId(TRIP_ID)).thenReturn(trip.toDto())

        val updatedDto = AlbumDto(null, "New Title", "New Desc", emptyList())
        val result = albumService.updateAlbum(ALBUM_ID, updatedDto)

        assertEquals("New Title", result.title)
        assertEquals("New Desc", result.description)
    }

    @Test
    fun `updateAlbum unauthorized if viewer or public`() {
        val trip = getTestTrip(ownerId = OTHER_USER_ID, visibility = Visibility.PUBLIC, collaborators = emptyList(), viewers = listOf(OWNER_ID))
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))

        val updatedDto = AlbumDto(null, "New Title", "New Desc", emptyList())

        assertThrows(AlbumUnauthorizedException::class.java) {
            albumService.updateAlbum(ALBUM_ID, updatedDto)
        }
        verify(albumRepository, never()).save(any())
    }

    @Test
    fun `getMediaFromAlbum success for viewer`() {
        val trip = getTestTrip(
            ownerId = VIEWER_ID,
            visibility = Visibility.PRIVATE,
            collaborators = emptyList(),
            viewers = listOf(OWNER_ID)
        )
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(defaultAlbum))

        albumService.getMediaFromAlbum(ALBUM_ID, MEDIA_ID)
    }

    @Test
    fun `getMediaFromAlbum unauthorized if private and not involved`() {
        val trip = getTestTrip(ownerId = OTHER_USER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))

        assertThrows(AlbumUnauthorizedException::class.java) {
            albumService.getMediaFromAlbum(ALBUM_ID, MEDIA_ID)
        }
    }

    @Test
    fun `getMediaFromAlbum throws AlbumWithoutMediaException if album is empty`() {
        val emptyAlbum = defaultAlbum.copy(media = mutableListOf())
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(emptyAlbum))

        assertThrows(AlbumWithoutMediaException::class.java) {
            albumService.getMediaFromAlbum(ALBUM_ID, MEDIA_ID)
        }
    }

    @Test
    fun `getMediaFromAlbum throws MediaNotFoundException if media is missing`() {
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(defaultAlbum)) // Album has MEDIA_ID

        assertThrows(MediaNotFoundException::class.java) {
            albumService.getMediaFromAlbum(ALBUM_ID, 999L) // Searching for a different ID
        }
    }

    @Test
    fun `addMediaToAlbum success for collaborator`() {
        val trip = getTestTrip(ownerId = OTHER_USER_ID, visibility = Visibility.PRIVATE, collaborators = listOf(OWNER_ID), viewers = emptyList())

        val initialAlbum = defaultAlbum.copy(media = mutableListOf())

        val mediaDtos = listOf(MediaDto(null, "new_file.png", null, "image", "png", 50L))
        val expectedSavedMediaId = 123L

        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(initialAlbum))

        `when`(albumRepository.save(any<Album>())).thenAnswer { invocation ->
            val albumToSave = invocation.arguments[0] as Album

            val mediaToSave = albumToSave.media.last()

            val mediaAfterSave = mediaToSave.copy(mediaId = expectedSavedMediaId)

            albumToSave.copy(
                media = (albumToSave.media.dropLast(1) + mediaAfterSave).toMutableList()
            )
        }

        val result = albumService.addMediaToAlbum(ALBUM_ID,  mediaDtos)

        assertEquals(1, result.media.size)
        val savedMediaDto = result.media.first()
        assertEquals("new_file.png", savedMediaDto.pathUrl)
        assertEquals(OWNER_ID, savedMediaDto.uploader, "Uploader must be authenticated user")
        assertEquals(expectedSavedMediaId, savedMediaDto.mediaId, "The generated mediaId must be returned")

        verify(albumRepository).save(any())
    }

    @Test
    fun `addMediaToAlbum unauthorized if viewer`() {
        val trip = getTestTrip(ownerId = OTHER_USER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = listOf(OWNER_ID))
        val mediaDtos = listOf(MediaDto(null, "x.jpg", null, "img", "jpg", 1L))
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))

        assertThrows(AlbumUnauthorizedException::class.java) {
            albumService.addMediaToAlbum(ALBUM_ID, mediaDtos)
        }
        verify(albumRepository, never()).save(any())
    }

    @Test
    fun `deleteMediaFromAlbum success for owner`() {
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        val album = defaultAlbum.copy(media = mutableListOf(defaultMedia)) // Ensure media is present

        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(album))
        `when`(albumRepository.save(any<Album>())).thenAnswer { it.arguments[0] } // Mock save to return the modified album

        albumService.deleteMediaFromAlbum(ALBUM_ID, MEDIA_ID)

        assertEquals(0, album.media.size)
    }

    @Test
    fun `deleteMediaFromAlbum unauthorized if viewer`() {
        val trip = getTestTrip(ownerId = OTHER_USER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = listOf(OWNER_ID))
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))

        assertThrows(AlbumUnauthorizedException::class.java) {
            albumService.deleteMediaFromAlbum(ALBUM_ID, MEDIA_ID)
        }
        verify(albumRepository, never()).save(any())
    }

    @Test
    fun `deleteMediaFromAlbum throws MediaNotFoundException`() {
        val trip = getTestTrip(ownerId = OWNER_ID, visibility = Visibility.PRIVATE, collaborators = emptyList(), viewers = emptyList())
        mockAlbumToTripLink()
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(trip))
        `when`(albumRepository.findById(ALBUM_ID)).thenReturn(Optional.of(defaultAlbum)) // Album contains MEDIA_ID

        assertThrows(MediaNotFoundException::class.java) {
            albumService.deleteMediaFromAlbum(ALBUM_ID, 999L) // ID that is not in the default album
        }
        verify(albumRepository, never()).save(any())
    }
}