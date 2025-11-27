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
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.ApplicationEventPublisher
import traversium.tripservice.db.model.Album
import traversium.tripservice.db.model.Media
import traversium.tripservice.db.model.Trip
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.*
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.TripEventType
import traversium.tripservice.security.BaseSecuritySetup
import traversium.tripservice.service.FirebaseService
import traversium.tripservice.service.TripService
import java.time.OffsetDateTime
import java.util.*
import traversium.tripservice.kafka.data.ReportingStreamData
import traversium.tripservice.kafka.publisher.NotificationPublisher

@ExtendWith(MockitoExtension::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TripServiceTest : BaseSecuritySetup() {

    @Mock
    private lateinit var tripRepository: TripRepository

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Mock
    private lateinit var notificationPublisher: NotificationPublisher

    @Mock
    private lateinit var firebaseService: FirebaseService

    @InjectMocks
    private lateinit var tripService: TripService

    private val TRIP_ID = 1L
    private val ALBUM_ID = 99L
    private val OTHER_ALBUM_ID = 100L
    private val OWNER_ID = firebaseId // Authenticated user
    private val OTHER_USER_ID = "firebase_other"
    private val COLLABORATOR_ID = "firebase_collab"
    private val VIEWER_ID = "firebase_viewer"

    private val defaultTrip = Trip(
        tripId = TRIP_ID,
        title = "My Test Trip",
        ownerId = OWNER_ID,
        visibility = Visibility.PRIVATE,
        createdAt = OffsetDateTime.now(),
        collaborators = mutableListOf(OWNER_ID),
        viewers = mutableListOf(),
        albums = mutableListOf()
    )

    @BeforeEach
    fun setUp() {
        setupDefaultFirebaseMocks()
    }

    private fun setupDefaultFirebaseMocks() {
        lenient().`when`(firebaseService.extractUidFromToken(token)).thenReturn(OWNER_ID)
    }

    @Test
    fun `createTrip success`() {
        val dto = TripDto(
            tripId = null,
            title = "New Adventure",
            ownerId = null, // ownerId is determined by context
            collaborators = listOf(COLLABORATOR_ID) // Adding an extra collaborator
        )

        `when`(tripRepository.save(any<Trip>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Trip).copy(tripId = TRIP_ID)
        }

        val result = tripService.createTrip(dto)

        assertEquals(TRIP_ID, result.tripId)
        assertEquals(OWNER_ID, result.ownerId, "Owner must be the authenticated user")

        assertEquals(2, result.collaborators.size)

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.TRIP_CREATED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `createTrip throws exception if title is null`() {
        val dto = TripDto(tripId = null, title = null)

        assertThrows(IllegalArgumentException::class.java) {
            tripService.createTrip(dto)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `getAllTrips success returns list of accessible trips`() {
        val accessibleTrips = listOf(
            defaultTrip.copy(tripId = 10L, title = "Owned"),
            defaultTrip.copy(tripId = 11L, title = "Collab", ownerId = OTHER_USER_ID)
        )

        `when`(tripRepository.findAllAccessibleTripsByUserId(OWNER_ID)).thenReturn(accessibleTrips)

        val result = tripService.getAllTrips()

        assertEquals(2, result.size)
        assertEquals("Owned", result[0].title)

        verify(tripRepository).findAllAccessibleTripsByUserId(OWNER_ID)
    }

    @Test
    fun `getAllTrips returns empty list when user has no trips`() {
        `when`(tripRepository.findAllAccessibleTripsByUserId(OWNER_ID)).thenReturn(emptyList())

        val result = tripService.getAllTrips()

        // Assert that an empty list is returned, not an exception thrown
        assertEquals(0, result.size)
        verify(tripRepository).findAllAccessibleTripsByUserId(OWNER_ID)
    }

    @Test
    fun `getByTripId success for owner`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))

        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)

        val result = tripService.getByTripId(TRIP_ID)

        assertEquals(TRIP_ID, result.tripId)
        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `getByTripId success for collaborator`() {
        val collabTrip = defaultTrip.copy(
            collaborators = mutableListOf(COLLABORATOR_ID, OWNER_ID),
            ownerId = OTHER_USER_ID
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(collabTrip))
        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)

        val result = tripService.getByTripId(TRIP_ID)

        assertEquals(TRIP_ID, result.tripId)
        assertEquals(OTHER_USER_ID, result.ownerId)
        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `getByTripId success for public trip`() {
        val publicTrip = defaultTrip.copy(
            visibility = Visibility.PUBLIC,
            ownerId = OTHER_USER_ID,
            collaborators = mutableListOf()
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(publicTrip))
        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)

        val result = tripService.getByTripId(TRIP_ID)

        assertEquals(TRIP_ID, result.tripId)
        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `getByTripId unauthorized if private and not involved`() {
        val privateTrip = defaultTrip.copy(
            ownerId = OTHER_USER_ID,
            collaborators = mutableListOf()
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(privateTrip))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.getByTripId(TRIP_ID)
        }
    }

    @Test
    fun `getByTripId not found`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.empty())

        assertThrows(TripNotFoundException::class.java) {
            tripService.getByTripId(TRIP_ID)
        }
    }

    @Test
    fun `getTripsByOwner success for self`() {
        val myOwnedTrips = listOf(defaultTrip.copy(tripId = 30L, title = "My Owned Trip"))

        // Authenticated user (OWNER_ID) is querying their own trips
        `when`(tripRepository.findByOwnerId(OWNER_ID)).thenReturn(myOwnedTrips)

        val result = tripService.getTripsByOwner(OWNER_ID)

        assertEquals(1, result.size)
        assertEquals("My Owned Trip", result[0].title)

        // Verification: Ensure the one-parameter method was called
        verify(tripRepository).findByOwnerId(eq(OWNER_ID))
        verify(tripRepository, never()).findByOwnerId(any<String>(), any<String>())
    }

    @Test
    fun `getTripsByOwner success for other user when authorized`() {
        val accessibleTrips = listOf(defaultTrip.copy(
            tripId = 31L,
            title = "Theirs I See",
            ownerId = OTHER_USER_ID,
            collaborators = mutableListOf(OWNER_ID)
        ))

        `when`(tripRepository.findByOwnerId(OTHER_USER_ID, OWNER_ID)).thenReturn(accessibleTrips)

        val result = tripService.getTripsByOwner(OTHER_USER_ID)

        assertEquals(1, result.size)
        assertEquals("Theirs I See", result[0].title)

        // Verification: Ensure the two-parameter authorized method was called
        verify(tripRepository).findByOwnerId(OTHER_USER_ID, OWNER_ID)
    }

    @Test
    fun `updateTrip success for owner`() {
        val updatedTitle = "Updated Title"
        val updatedDto = defaultTrip.toDto().copy(title = updatedTitle, tripId = TRIP_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))
        `when`(tripRepository.save(any<Trip>())).thenAnswer { it.arguments[0] }

        val result = tripService.updateTrip(updatedDto)

        assertEquals(updatedTitle, result.title)

        verify(tripRepository).save(argThat { trip: Trip ->
            trip.title == updatedTitle && trip.createdAt == defaultTrip.createdAt
        })
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.TRIP_UPDATED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `updateTrip unauthorized if not owner`() {
        val tripOwnedByOther = defaultTrip.copy(ownerId = OTHER_USER_ID)
        val updatedDto = defaultTrip.toDto().copy(tripId = TRIP_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.updateTrip(updatedDto)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteTrip success for owner`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))

        tripService.deleteTrip(TRIP_ID)

        verify(tripRepository).delete(defaultTrip)
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.TRIP_DELETED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `deleteTrip unauthorized if not owner`() {
        val tripOwnedByOther = defaultTrip.copy(ownerId = OTHER_USER_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.deleteTrip(TRIP_ID)
        }

        verify(tripRepository, never()).delete(any())
    }

    @Test
    fun `getTripsByCollaborator success for self`() {
        val myTrips = listOf(defaultTrip.copy(tripId = 20L, title = "My Collab"))

        `when`(tripRepository.findByCollaboratorId(OWNER_ID)).thenReturn(myTrips)

        val result = tripService.getTripsByCollaborator(OWNER_ID)

        assertEquals(1, result.size)
        assertEquals("My Collab", result[0].title)

        verify(tripRepository).findByCollaboratorId(OWNER_ID)
        verify(tripRepository, never()).findByCollaboratorId(any(), any())
    }

    @Test
    fun `getTripsByCollaborator success for other user when authorized`() {
        val otherUsersCollabs = listOf(defaultTrip.copy(tripId = 21L, title = "Theirs I See"))
        val otherUserId = "other_collab_user"

        `when`(tripRepository.findByCollaboratorId(otherUserId, OWNER_ID)).thenReturn(otherUsersCollabs)

        val result = tripService.getTripsByCollaborator(otherUserId)

        assertEquals(1, result.size)
        assertEquals("Theirs I See", result[0].title)

        verify(tripRepository).findByCollaboratorId(otherUserId, OWNER_ID)
        verify(tripRepository, never()).findByCollaboratorId(eq(OWNER_ID))
    }

    @Test
    fun `getTripsByCollaborator throws not found if list is empty`() {
        val otherUserId = "other_collab_user"

        `when`(tripRepository.findByCollaboratorId(any<String>(), any<String>())).thenReturn(emptyList())

        assertThrows(TripNotFoundException::class.java) {
            tripService.getTripsByCollaborator(otherUserId)
        }
    }

    @Test
    fun `addCollaboratorToTrip success for owner`() {
        val newCollaboratorId = "new_collab"
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))
        `when`(tripRepository.save(any<Trip>())).thenAnswer { it.arguments[0] }

        tripService.addCollaboratorToTrip(TRIP_ID, newCollaboratorId)

        assertEquals(2, defaultTrip.collaborators.size)
        assertEquals(true, defaultTrip.collaborators.contains(newCollaboratorId))

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.COLLABORATOR_ADDED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `addCollaboratorToTrip unauthorized if not owner`() {
        val newCollaboratorId = "new_collab"
        val tripOwnedByOther = defaultTrip.copy(ownerId = OTHER_USER_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.addCollaboratorToTrip(TRIP_ID, newCollaboratorId)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteCollaboratorFromTrip success for owner`() {
        val tripWithCollaborator = defaultTrip.copy(
            collaborators = mutableListOf(OWNER_ID, COLLABORATOR_ID)
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripWithCollaborator))
        `when`(tripRepository.save(any<Trip>())).thenAnswer { it.arguments[0] }

        tripService.deleteCollaboratorFromTrip(TRIP_ID, COLLABORATOR_ID)

        assertEquals(1, tripWithCollaborator.collaborators.size)
        assertEquals(false, tripWithCollaborator.collaborators.contains(COLLABORATOR_ID))

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.COLLABORATOR_DELETED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `deleteCollaboratorFromTrip unauthorized if not owner`() {
        val tripOwnedByOther = defaultTrip.copy(ownerId = OTHER_USER_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.deleteCollaboratorFromTrip(TRIP_ID, COLLABORATOR_ID)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteCollaboratorFromTrip throws if collaborator not found`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))

        assertThrows(TripWithoutCollaboratorException::class.java) {
            tripService.deleteCollaboratorFromTrip(TRIP_ID, COLLABORATOR_ID)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteCollaboratorFromTrip throws if attempting to delete owner`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.deleteCollaboratorFromTrip(TRIP_ID, OWNER_ID)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `getTripsByViewer success returns trips where user is viewer`() {
        val viewedTrips = listOf(defaultTrip.copy(tripId = 40L, title = "Viewed Trip", viewers = mutableListOf(OWNER_ID)))

        `when`(tripRepository.findByViewerId(OWNER_ID)).thenReturn(viewedTrips)

        val result = tripService.getTripsByViewer()

        assertEquals(1, result.size)
        assertEquals("Viewed Trip", result[0].title)

        verify(tripRepository).findByViewerId(OWNER_ID)
    }

    @Test
    fun `getTripsByViewer throws not found if list is empty`() {
        `when`(tripRepository.findByViewerId(OWNER_ID)).thenReturn(emptyList())

        assertThrows(TripNotFoundException::class.java) {
            tripService.getTripsByViewer()
        }
    }

    @Test
    fun `addViewerToTrip success for owner`() {
        val newViewerId = "new_viewer"
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))
        `when`(tripRepository.save(any<Trip>())).thenAnswer { it.arguments[0] }

        tripService.addViewerToTrip(TRIP_ID, newViewerId)

        assertEquals(1, defaultTrip.viewers.size)
        assertEquals(true, defaultTrip.viewers.contains(newViewerId))

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.VIEWER_ADDED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `addViewerToTrip unauthorized if not owner`() {
        val newViewerId = "new_viewer"
        val tripOwnedByOther = defaultTrip.copy(ownerId = OTHER_USER_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.addViewerToTrip(TRIP_ID, newViewerId)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteViewerFromTrip success for owner`() {
        val viewerToDeleteId = "viewer_to_delete"
        val tripWithViewer = defaultTrip.copy(
            viewers = mutableListOf(viewerToDeleteId)
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripWithViewer))
        `when`(tripRepository.save(any<Trip>())).thenAnswer { it.arguments[0] }

        tripService.deleteViewerFromTrip(TRIP_ID, viewerToDeleteId)

        assertEquals(0, tripWithViewer.viewers.size)
        assertEquals(false, tripWithViewer.viewers.contains(viewerToDeleteId))

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? TripEvent
                action != null &&
                        action.eventType == TripEventType.VIEWER_DELETED &&
                        action.tripId == TRIP_ID
            }
        )
    }

    @Test
    fun `deleteViewerFromTrip unauthorized if not owner`() {
        val viewerToDeleteId = "viewer_to_delete"
        val tripOwnedByOther = defaultTrip.copy(ownerId = OTHER_USER_ID)

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.deleteViewerFromTrip(TRIP_ID, viewerToDeleteId)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteViewerFromTrip throws if viewer not found`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))

        assertThrows(TripWithoutViewerException::class.java) {
            tripService.deleteViewerFromTrip(TRIP_ID, VIEWER_ID)
        }

        verify(tripRepository, never()).save(any())
    }

    private val defaultAlbum = Album(albumId = ALBUM_ID, title = "Test Album", media = mutableListOf())
    private val tripWithAlbum = defaultTrip.copy(albums = mutableListOf(defaultAlbum))

    @Test
    fun `getAlbumFromTrip success for owner`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripWithAlbum))

        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)

        val result = tripService.getAlbumFromTrip(TRIP_ID, ALBUM_ID)

        assertEquals(ALBUM_ID, result.albumId)
        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `getAlbumFromTrip unauthorized if private and not involved`() {
        val privateTripOwnedByOther = defaultTrip.copy(
            ownerId = OTHER_USER_ID,
            collaborators = mutableListOf(),
            albums = mutableListOf(defaultAlbum)
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(privateTripOwnedByOther))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.getAlbumFromTrip(TRIP_ID, ALBUM_ID)
        }
    }

    @Test
    fun `getAlbumFromTrip throws not found if album is missing`() {
        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(defaultTrip))

        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)

        assertThrows(TripWithoutAlbumsException::class.java) {
            tripService.getAlbumFromTrip(TRIP_ID, ALBUM_ID)
        }

        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `addAlbumToTrip success for collaborator`() {
        val albumDto = AlbumDto(albumId = ALBUM_ID, title = "New Album")
        val collabTrip = defaultTrip.copy(
            collaborators = mutableListOf(OWNER_ID, OTHER_USER_ID),
            albums = mutableListOf()
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(collabTrip))
        `when`(tripRepository.save(any<Trip>())).thenAnswer { it.arguments[0] }

        val result = tripService.addAlbumToTrip(TRIP_ID, albumDto)

        assertEquals(1, result.albums.size)
        assertEquals(albumDto.title, result.albums.first().title)

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? AlbumEvent
                action != null &&
                        action.eventType == AlbumEventType.ALBUM_CREATED &&
                        action.albumId == ALBUM_ID
            }
        )
    }

    @Test
    fun `addAlbumToTrip unauthorized if not collaborator`() {
        val albumDto = AlbumDto(albumId = ALBUM_ID, title = "New Album")
        val unauthorizedTrip = defaultTrip.copy(
            ownerId = OTHER_USER_ID,
            collaborators = mutableListOf(OTHER_USER_ID)
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(unauthorizedTrip))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.addAlbumToTrip(TRIP_ID, albumDto)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteAlbumFromTrip success for collaborator`() {
        val tripWithAlbum = defaultTrip.copy(
            collaborators = mutableListOf(OWNER_ID),
            albums = mutableListOf(defaultAlbum.copy(albumId = ALBUM_ID))
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripWithAlbum))

        tripService.deleteAlbumFromTrip(TRIP_ID, ALBUM_ID)

        assertEquals(0, tripWithAlbum.albums.size)

        verify(tripRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat { event: ReportingStreamData ->
                val action = event.action as? AlbumEvent
                action != null &&
                        action.eventType == AlbumEventType.ALBUM_DELETED &&
                        action.albumId == ALBUM_ID
            }
        )
    }

    @Test
    fun `deleteAlbumFromTrip unauthorized if not collaborator`() {
        val unauthorizedTrip = defaultTrip.copy(
            ownerId = OTHER_USER_ID,
            collaborators = mutableListOf(OTHER_USER_ID),
            albums = mutableListOf(defaultAlbum)
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(unauthorizedTrip))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.deleteAlbumFromTrip(TRIP_ID, ALBUM_ID)
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `deleteAlbumFromTrip throws not found if album is missing`() {
        val tripWithDifferentAlbum = defaultTrip.copy(
            collaborators = mutableListOf(OWNER_ID),
            albums = mutableListOf(defaultAlbum.copy(albumId = OTHER_ALBUM_ID)) // Album 100 is present
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripWithDifferentAlbum))

        assertThrows(AlbumNotFoundException::class.java) {
            tripService.deleteAlbumFromTrip(TRIP_ID, ALBUM_ID) // Trying to delete Album 99
        }

        verify(tripRepository, never()).save(any())
    }

    @Test
    fun `getAllMediaFromTrip success returns all media paths`() {
        val media1 = Media(pathUrl = "url/path/1")
        val media2 = Media(pathUrl = "url/path/2")

        val albumA = Album(albumId = 1L, title = "A", media = mutableListOf(media1))
        val albumB = Album(albumId = 2L, title = "B", media = mutableListOf(media2))

        val tripWithMedia = defaultTrip.copy(
            albums = mutableListOf(albumA, albumB)
        )

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(tripWithMedia))
        `when`(tripRepository.isUserAuthorizedToView(TRIP_ID, OWNER_ID)).thenReturn(true)

        val result = tripService.getAllMediaFromTrip(TRIP_ID)

        assertEquals(2, result.size)
        assertEquals("url/path/1", result[0])
        assertEquals("url/path/2", result[1])
        verify(tripRepository).isUserAuthorizedToView(TRIP_ID, OWNER_ID)
    }

    @Test
    fun `getAllMediaFromTrip unauthorized if private and not involved`() {
        val unauthorizedTrip = defaultTrip.copy(ownerId = OTHER_USER_ID, collaborators = mutableListOf())

        `when`(tripRepository.findById(TRIP_ID)).thenReturn(Optional.of(unauthorizedTrip))

        assertThrows(TripUnauthorizedException::class.java) {
            tripService.getAllMediaFromTrip(TRIP_ID)
        }
    }

}