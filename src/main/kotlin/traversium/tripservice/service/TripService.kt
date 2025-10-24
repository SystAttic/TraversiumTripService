package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.db.model.Trip
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.db.repository.TripRepository
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
        tripRepository.findByOwner(ownerId).map { it.toDto() }

    fun createTrip(dto: TripDto): TripDto {
        val trip = tripRepository.save(dto.toTrip())

        // Kafka event - Trip CREATE
        eventPublisher.publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_CREATED,
                tripId = trip.tripId,
                owner = trip.owner,
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
                owner = trip.owner,
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
                owner = updated.owner,
            )
        )

        return tripRepository.save(mergedTrip).toDto()
    }

    fun getTripsByCollaborator(collaboratorId: String): List<TripDto>{
        val trips = tripRepository.findByCollaborator(collaboratorId)
        return trips.map { it.toDto() }
    }

    // TODO - dodaj (tudi na drugih Service) endpointe, ki so še potrebni
}
