package traversium.tripservice.service

import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.db.repository.MediaRepository
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.exceptions.MediaUnauthorizedException

@Service
@Transactional
class MediaService(
    private val mediaRepository: MediaRepository,
    private val firebaseService: FirebaseService,
    private val tripRepository: TripRepository
) {
    private fun getFirebaseIdFromContext() =
        firebaseService.extractUidFromToken(SecurityContextHolder.getContext().authentication.credentials as String)

    private fun authorizeView(mediaId: Long, firebaseId: String) {
        val tripId = tripRepository.findTripIdByMediaId(mediaId)
            .orElseThrow { MediaNotFoundException(mediaId) } // If media has no trip, treat it as not found or invalid.

        val isAuthorized = tripRepository.isUserAuthorizedToView(tripId, firebaseId)

        if (!isAuthorized) {
            throw MediaUnauthorizedException("User $firebaseId is not authorized to view media $mediaId.")
        }
    }

    fun getAllMedia(): List<MediaDto> {
        val firebaseId = getFirebaseIdFromContext()

        val media = mediaRepository.findAllAccessibleMediaByUserId(firebaseId)

        if (media.isEmpty()) {
            throw MediaNotFoundException(0) // 0 implies general 'no accessible media'
        }
        return media.map { it.toDto() }
    }

    fun getAllMedia(offset: Int, limit: Int): List<MediaDto> {
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)

        val media = mediaRepository.findAllAccessibleMediaByUserId(firebaseId, pageable)

        if (media.isEmpty()) {
            throw MediaNotFoundException(0) // 0 implies general 'no accessible media'
        }
        return media.map { it.toDto() }
    }

    fun getMediaById(mediaId: Long): MediaDto {
        val firebaseId = getFirebaseIdFromContext()

        authorizeView(mediaId, firebaseId)

        return mediaRepository.findById(mediaId).orElseThrow { MediaNotFoundException(mediaId) }.toDto()
    }

    fun getMediaByUploader(uploaderId: String): List<MediaDto> {
        val firebaseId = getFirebaseIdFromContext()

        val media = mediaRepository.findAccessibleMediaByUploader(uploaderId, firebaseId)

        if (media.isEmpty()) {
            throw MediaNotFoundException(0)
        }
        return media.map { it.toDto() }
    }

    fun getMediaByUploader(uploaderId: String, offset: Int, limit: Int): List<MediaDto> {
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)

        val media = mediaRepository.findAccessibleMediaByUploader(uploaderId, firebaseId, pageable)

        if (media.isEmpty()) {
            throw MediaNotFoundException(0)
        }
        return media.map { it.toDto() }
    }

}
