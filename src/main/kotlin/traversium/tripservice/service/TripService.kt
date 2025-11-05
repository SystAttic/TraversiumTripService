package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.db.model.Album
import traversium.tripservice.db.model.Trip
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
) {

    fun getAllTrips(): List<TripDto> =
        tripRepository.findAll().map { it.toDto() }

    fun getByTripId(tripId: Long): TripDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }
        return trip.toDto()
    }

    fun getTripsByOwner(ownerId: String): List<TripDto> =
        tripRepository.findByOwnerId(ownerId).map { it.toDto() }

    private fun validateCollaborator(trip: Trip, collaboratorId: String) {
        if (trip.collaborators.contains(collaboratorId) || trip.ownerId == collaboratorId) {
            throw TripHasCollaboratorException(trip.tripId!!, collaboratorId)
        }
    }

    private fun validateViewer(trip: Trip, viewerId: String) {
        if (trip.viewers.contains(viewerId) || trip.ownerId == viewerId) {
            throw TripHasViewerException(trip.tripId!!, viewerId)
        }
    }

    @Transactional
    fun createTrip(dto: TripDto): TripDto {
        if (dto.ownerId == null || dto.title == null || dto.tripId != null) {
            throw IllegalArgumentException("Owner ID and title cannot be null, new trip cannot have tripId")
        }

        val trip = dto.toTrip().copy(
            albums = mutableListOf(),
            collaborators = mutableListOf(),
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
                ownerId = saved.ownerId,
            )
        )
        return saved.toDto()
    }

    @Transactional
    fun deleteTrip(tripId: Long) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

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
        val mergedTrip = existingTrip.copy(
            title = updated.title ?: existingTrip.title,
            description = updated.description ?: existingTrip.description,
            coverPhotoUrl = updated.coverPhotoUrl ?: existingTrip.coverPhotoUrl,
            visibility = updated.visibility ?: existingTrip.visibility,
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

    fun getTripsByCollaborator(collaboratorId: String): List<TripDto>{
        val trips = tripRepository.findByCollaboratorId(collaboratorId)
        return trips.map { it.toDto() }
    }

    @Transactional
    fun addCollaboratorToTrip(tripId: Long, collaboratorId: String): TripDto {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { TripNotFoundException(tripId) }

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

    fun getTripsByViewer(viewerId: String): List<TripDto>{
        val trips = tripRepository.findByViewerId(viewerId)
        return trips.map { it.toDto() }
    }

    @Transactional
    fun addViewerToTrip(tripId: Long, viewerId: String) :TripDto {
        val trip = tripRepository.findById(tripId)
            .orElseThrow { TripNotFoundException(tripId) }

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

    fun getAlbumFromTrip(tripId: Long, albumId: Long): AlbumDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }
        if(trip.albums.isEmpty())
            throw TripWithoutAlbumsException(tripId)
        else
            return trip.albums.first { it.albumId == albumId }.toDto()
    }

    @Transactional
    fun addAlbumToTrip(tripId: Long, dto: AlbumDto) : TripDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }
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

    @Transactional
    fun removeBlockedUserRelations(blockerId: String, blockedId: String) : String {
        val ownedTrips = tripRepository.findByOwnerId(blockerId)
        ownedTrips.forEach { trip ->
            val changed = trip.collaborators.remove(blockedId) or trip.viewers.remove(blockedId)
            if (changed) tripRepository.save(trip)
        }

        val blockedTrips = tripRepository.findByOwnerId(blockedId)
        blockedTrips.forEach { trip ->
            val changed = trip.collaborators.remove(blockerId) or trip.viewers.remove(blockerId)
            if (changed) tripRepository.save(trip)
        }

        return "Removed blocked user relations from trips."
    }
}
