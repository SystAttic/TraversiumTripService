package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.TripEventType

@Service
@Transactional
class TripService(
    private val tripRepository: TripRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun getAllTrips(): List<TripDto> =
        tripRepository.findAll().map { it.toDto() }

    fun getByTripId(tripId: Long): TripDto =
        tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }.toDto()

    fun getTripsByOwner(ownerId: String): List<TripDto> =
        tripRepository.findByOwnerId(ownerId).map { it.toDto() }

    fun createTrip(dto: TripDto): TripDto {
        val trip = tripRepository.save(dto.toTrip())

        // Kafka event - Trip CREATE
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_CREATED,
                tripId = trip.tripId,
                ownerId = trip.ownerId,
            )
        )
        return trip.toDto()
    }

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

    fun updateTrip(tripId: Long, updated: TripDto): TripDto {
        val existingTrip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }
        val mergedTrip = existingTrip.copy(
            title = updated.title,
            description = updated.description,
            coverPhotoUrl = updated.coverPhotoUrl ?: existingTrip.coverPhotoUrl,
            collaborators = updated.collaborators,
            viewers = updated.viewers
        )
        // Kafka event - Trip UPDATE
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_UPDATED,
                tripId = updated.tripId,
                ownerId = updated.ownerId,
            )
        )

        return tripRepository.save(mergedTrip).toDto()
    }

    fun getTripsByCollaborator(collaboratorId: String): List<TripDto>{
        val trips = tripRepository.findByCollaborator(collaboratorId)
        return trips.map { it.toDto() }
    }

    fun addAlbumToTrip(tripId: Long, dto: AlbumDto) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }
        trip.albums.add(dto.toAlbum())

        tripRepository.save(trip)
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_CREATED,
                albumId = dto.albumId,
                title = dto.title,
            )
        )
    }

    // TODO - dodaj (tudi na drugih Service) endpointe, ki so še potrebni
}
