package traversium.tripservice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.db.repository.TripRepository

@Service
class TripCleanupService(
    private val tripRepository: TripRepository
) {
    @Transactional
    fun removeBlockedUserRelations(blockerId: String, blockedId: String) {
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

        println("Removed relations between $blockerId and $blockedId")
    }
}
