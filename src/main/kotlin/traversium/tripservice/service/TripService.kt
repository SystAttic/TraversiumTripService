package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.db.model.Trip
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.*
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.TripEventType

@Service
@Transactional
class TripService(
    private val tripRepository: TripRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val firebaseService: FirebaseService
) {
    private fun validateCollaborator(trip: Trip, collaboratorId: String) {
        if (trip.collaborators.contains(collaboratorId)) {
            throw TripHasCollaboratorException(trip.tripId!!, collaboratorId)
        }
    }

    private fun validateViewer(trip: Trip, viewerId: String) {
        if (trip.viewers.contains(viewerId) || trip.ownerId == viewerId) {
            throw TripHasViewerException(trip.tripId!!, viewerId)
        }
    }

    private fun isUserAuthorizedToView(trip: Trip, userId: String): Boolean {
        // Check if the trip is PUBLIC, or if the user is a collaborator, or a viewer
        return trip.visibility == Visibility.PUBLIC ||
                trip.collaborators.contains(userId) ||
                trip.viewers.contains(userId)
    }

    private fun getFirebaseIdFromContext(): String =
        firebaseService.extractUidFromToken(SecurityContextHolder.getContext().authentication.credentials as String)

    /*
    *   <--- Trips --->
    */

    // where user is owner, viewer or collaborator
    fun getAllTrips(): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        val trips = tripRepository.findAllAccessibleTripsByUserId(firebaseId) // get all trips, where the user is owner, collaborator or viewer

        return trips.map { it.toDto() }
    }

    fun getByTripId(tripId: Long): TripDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()

        if (!isUserAuthorizedToView(trip, firebaseId)) {
            throw TripUnauthorizedException("User is not authorized to view trip ID $tripId.")
        }

        return trip.toDto()
    }

    // trips where user is owner
    fun getTripsByOwner(ownerId: String): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        return if (ownerId == firebaseId)
            tripRepository.findByOwnerId(ownerId).map { it.toDto() }
        else
            tripRepository.findByOwnerId(ownerId, firebaseId).map { it.toDto() }
    }

    // trips where user is owner or collaborator
    fun getTripsByOwnerOrCollaborator(userId: String): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        if (userId == firebaseId)
            return tripRepository.findByOwnerOrCollaborator(userId).map { it.toDto() }
        else
            throw TripUnauthorizedException("User is not authorized to perform this operation.")
    }

    @Transactional
    fun createTrip(dto: TripDto): TripDto {
        if (
            //dto.ownerId == null ||
            dto.title == null || dto.tripId != null) {
            throw IllegalArgumentException("Owner ID and title cannot be null, new trip cannot have tripId")
        }

        val firebaseId = getFirebaseIdFromContext()

        val trip = dto.toTrip().copy(
            ownerId = firebaseId,
            albums = mutableListOf(),
            collaborators = mutableListOf(firebaseId),
            viewers = mutableListOf()
        )

        // Validate collaborators
        dto.collaborators.forEach { collaboratorId ->
            validateCollaborator(trip, collaboratorId) // if it fails, throw exception
            trip.collaborators.add(collaboratorId)
        }

        // Validate viewers
        dto.viewers.forEach { viewerId ->
            validateViewer(trip, viewerId) // if it fails, throw exception
            trip.viewers.add(viewerId)
        }

        val saved = tripRepository.save(trip)

        // Kafka event - Trip CREATE
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_CREATED,
                tripId = saved.tripId,
                ownerId = firebaseId,
            )
        )
        return saved.toDto()
    }

    @Transactional
    fun deleteTrip(tripId: Long) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()

        if (firebaseId != trip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        // Kafka event - Trip DELETE
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_DELETED,
                tripId = trip.tripId,
                ownerId = trip.ownerId,
            )
        )
        tripRepository.delete(trip)
    }

    @Transactional
    fun updateTrip(updated: TripDto): TripDto {
        if(updated.tripId == null)
            throw InvalidTripDataException()

        val existingTrip = tripRepository.findById(updated.tripId).orElseThrow { TripNotFoundException(updated.tripId) }

        val firebaseId = getFirebaseIdFromContext()

        if (firebaseId != existingTrip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        val mergedTrip = existingTrip.copy(
            title = updated.title ?: existingTrip.title,
            description = updated.description ?: existingTrip.description,
            coverPhotoUrl = updated.coverPhotoUrl ?: existingTrip.coverPhotoUrl,
            visibility = updated.visibility ?: existingTrip.visibility,
            createdAt = existingTrip.createdAt
        )
        // Kafka event - Trip UPDATE
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_UPDATED,
                tripId = mergedTrip.tripId,
                ownerId = mergedTrip.ownerId,
            )
        )

        return tripRepository.save(mergedTrip).toDto()
    }

    /*
    *   <-- Collaborators -->
    */
    fun getTripsByCollaborator(collaboratorId: String): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        return if (collaboratorId == firebaseId)
            tripRepository.findByCollaboratorId(firebaseId).map { it.toDto() }
        else
            tripRepository.findByCollaboratorId(collaboratorId, firebaseId).map { it.toDto() }
    }

    @Transactional
    fun addCollaboratorToTrip(tripId: Long, collaboratorId: String): TripDto {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()

        if (firebaseId != trip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        validateCollaborator(trip, collaboratorId)

        trip.collaborators.add(collaboratorId)

        val saved = tripRepository.save(trip)

        // Kafka event - Collaborator ADD
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.COLLABORATOR_ADDED,
                tripId = saved.tripId,
                ownerId = saved.ownerId
            )
        )
        return saved.toDto()
    }

    @Transactional
    fun deleteCollaboratorFromTrip(tripId: Long, collaboratorId: String) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()

        if (firebaseId != trip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")
        if (collaboratorId == trip.ownerId)
            throw TripUnauthorizedException("Owner cannot be deleted.")


        if (trip.collaborators.contains(collaboratorId)) {
            // Kafka event - Collaborator DELETE
            eventPublisher.publishEvent(
                TripEvent(
                    eventType = TripEventType.COLLABORATOR_DELETED,
                    tripId = trip.tripId,
                    ownerId = trip.ownerId,
                )
            )
            trip.collaborators.remove(collaboratorId)
            tripRepository.save(trip)
        } else throw TripWithoutCollaboratorException(tripId,collaboratorId)
    }

    /*
    *   <-- Viewers -->
    */
    // get trips where user is a viewer
    fun getTripsByViewer(): List<TripDto>{
        val firebaseId = getFirebaseIdFromContext()
        val trips = tripRepository.findByViewerId(firebaseId)

        return trips.map { it.toDto() }
    }

    @Transactional
    fun addViewerToTrip(tripId: Long, viewerId: String) :TripDto {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (firebaseId != trip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        validateViewer(trip, viewerId)

        trip.viewers.add(viewerId)

        val saved = tripRepository.save(trip)

        // Kafka event - Viewer ADD
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.VIEWER_ADDED,
                tripId = saved.tripId,
                ownerId = saved.ownerId
            )
        )
        return saved.toDto()
    }

    @Transactional
    fun deleteViewerFromTrip(tripId: Long, viewerId: String) {
        val trip = tripRepository.findById(tripId)
        .orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (firebaseId != trip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")


        if (trip.viewers.contains(viewerId)) {
            // Kafka event - Viewer DELETE
            eventPublisher.publishEvent(
                TripEvent(
                    eventType = TripEventType.VIEWER_DELETED,
                    tripId = trip.tripId,
                    ownerId = trip.ownerId,
                )
            )
            trip.viewers.remove(viewerId)
            tripRepository.save(trip)
        } else throw TripWithoutViewerException(tripId,viewerId)
    }


    /*
    *   <-- Albums -->
    */
    fun getAlbumFromTrip(tripId: Long, albumId: Long): AlbumDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (!isUserAuthorizedToView(trip, firebaseId)) {
            throw TripUnauthorizedException("User is not authorized to view trip ID $tripId.")
        }

        if(trip.albums.isEmpty())
            throw TripWithoutAlbumsException(tripId)
        else
            return trip.albums.first { it.albumId == albumId }.toDto()
    }

    @Transactional
    fun addAlbumToTrip(tripId: Long, dto: AlbumDto) : TripDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (firebaseId != trip.ownerId || !trip.collaborators.contains(firebaseId))
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        trip.albums.add(dto.toAlbum())

        // Kafka event - Album CREATE
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_CREATED,
                albumId = dto.albumId,
                title = dto.title,
            )
        )
        return tripRepository.save(trip).toDto()
    }

    @Transactional
    fun deleteAlbumFromTrip(tripId: Long, albumId: Long) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (firebaseId != trip.ownerId || !trip.collaborators.contains(firebaseId)) {
            throw TripUnauthorizedException("User is not authorized to perform this operation.")
        }

        if(trip.albums.any { it.albumId == albumId }) {
            val album = trip.albums.find { it.albumId == albumId }

            // Kafka event - Album DELETE
            eventPublisher.publishEvent(
                AlbumEvent(
                    eventType = AlbumEventType.ALBUM_DELETED,
                    albumId = album!!.albumId,
                    title = album.title
                )
            )
            trip.albums.remove(album)
            tripRepository.save(trip)
        }else
            throw AlbumNotFoundException(albumId)

    }

    /*
    *   <-- Other -->
    */
    @Transactional
    fun removeBlockedUserRelations(blockerId: String, blockedId: String) : String {
        val ownedTrips = tripRepository.findOwnedForBlocking(blockerId, blockedId)
        ownedTrips.forEach { trip ->
            val changedCollaborator = trip.collaborators.remove(blockedId)
            val changedViewer = trip.viewers.remove(blockedId)

            if (changedCollaborator || changedViewer) tripRepository.save(trip)
        }

        val blockedTrips = tripRepository.findOwnedForBlocking(blockedId, blockerId)
        blockedTrips.forEach { trip ->
            val changedCollaborator = trip.collaborators.remove(blockerId)
            val changedViewer = trip.viewers.remove(blockerId)

            if (changedCollaborator || changedViewer) tripRepository.save(trip)
        }

        return "SUCCESS"
    }

    fun getAllMediaFromTrip(tripId: Long): List<String> {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (!isUserAuthorizedToView(trip, firebaseId)) {
            throw TripUnauthorizedException("User is not authorized to view trip ID $tripId.")
        }

        return trip.albums
            .flatMap { album -> album.media.mapNotNull { it.pathUrl } }
    }

}
