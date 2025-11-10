package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.AlbumUnauthorizedException
import traversium.tripservice.exceptions.AlbumWithoutMediaException
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.MediaEvent
import traversium.tripservice.kafka.data.MediaEventType

@Service
@Transactional
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val tripRepository: TripRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val firebaseService: FirebaseService
) {
    private fun getFirebaseIdFromContext(): String =
        firebaseService.extractUidFromToken(SecurityContextHolder.getContext().authentication.credentials as String)

    private fun getTripIdByAlbumId(albumId: Long): Long {
        return tripRepository.findTripIdByAlbumId(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) } // Assuming 0 for generic lookup failure
    }

    private fun authorizeView(tripId: Long, firebaseId: String) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        // Use the same authorization logic as TripService's isUserAuthorizedToView
        val isAuthorized = trip.visibility == Visibility.PUBLIC ||
                trip.collaborators.contains(firebaseId) ||
                trip.viewers.contains(firebaseId) ||
                trip.ownerId == firebaseId

        if (!isAuthorized) {
            throw AlbumUnauthorizedException("User is not authorized to view album.")
        }
    }

    private fun authorizeModify(tripId: Long, firebaseId: String) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val isAuthorized = trip.collaborators.contains(firebaseId) || trip.ownerId == firebaseId

        if (!isAuthorized) {
            throw AlbumUnauthorizedException("User is not authorized to modify album.")
        }
    }


    fun getAllAlbums(): List<AlbumDto> {
        val firebaseId = getFirebaseIdFromContext()
        // This repository method is already assumed to handle security filtering.
        val albums = albumRepository.findAllAccessibleAlbumsByUserId(firebaseId)

        if(albums.isEmpty())
            throw AlbumNotFoundException(0)

        return albums.map { it.toDto() }
    }

    fun getByAlbumId(albumId: Long): AlbumDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeView(tripId, firebaseId)

        return albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }
            .toDto()
    }

    @Transactional
    fun updateAlbum(albumId: Long, dto: AlbumDto): AlbumDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeModify(tripId, firebaseId)

        val existingAlbum = albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }

        val updatedAlbum = existingAlbum.copy(
            title = dto.title ?: existingAlbum.title,
            description = dto.description ?: existingAlbum.description,
        )
        // Kafka event - Album UPDATE
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_UPDATED,
                albumId = updatedAlbum.albumId,
                title = updatedAlbum.title,
            )
        )
        return albumRepository.save(updatedAlbum).toDto()
    }

    fun getMediaFromAlbum(albumId: Long, mediaId: Long): MediaDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeView(tripId, firebaseId)

        val album = albumRepository.findById(albumId).orElseThrow { AlbumNotFoundException(albumId) }

        if(album.media.isEmpty())
            throw AlbumWithoutMediaException(albumId)
        else {
            return try {
                album.media.first { it.mediaId == mediaId }.toDto()
            } catch (_: NoSuchElementException) {
                throw MediaNotFoundException(mediaId)
            }
        }
    }

    @Transactional
    fun addMediaToAlbum(albumId: Long, dto: MediaDto) : AlbumDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeModify(tripId, firebaseId)

        val media = dto.copy(
            uploader = firebaseId
        )

        val album = albumRepository.findById(albumId).orElseThrow{ AlbumNotFoundException(albumId) }

        try {
            album.media.add(media.toMedia())
        } catch (_: Exception) {
            throw IllegalArgumentException("Failed to add media to album list.")
        }

        val savedAlbum = try {
            albumRepository.save(album)
        } catch (_: Exception) {
            throw IllegalArgumentException("Failed to save album with new media.")
        }

        val savedMedia = savedAlbum.media
            .firstOrNull { it.pathUrl == media.pathUrl && it.uploader == firebaseId }
            ?: throw IllegalArgumentException("Media not found in saved album, ID generation failed.")

        eventPublisher.publishEvent(
            MediaEvent(
                eventType = MediaEventType.MEDIA_ADDED,
                mediaId = savedMedia.mediaId,
                pathUrl = savedMedia.pathUrl,
            )
        )

        return savedAlbum.toDto()
    }

    @Transactional
    fun deleteMediaFromAlbum(albumId: Long, mediaId: Long) {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeModify(tripId, firebaseId)

        val album = albumRepository.findById(albumId).orElseThrow { AlbumNotFoundException(albumId) }

        if(album.media.any { it.mediaId == mediaId }) {
            val media = album.media.find { it.mediaId == mediaId }
            if (media == null) {
                throw MediaNotFoundException(mediaId)
            }
            // Kafka event - Media DELETE
            eventPublisher.publishEvent(
                MediaEvent(
                    eventType = MediaEventType.MEDIA_DELETED,
                    mediaId = media.mediaId,
                    pathUrl = media.pathUrl,
                    )
            )
            try {
                album.media.remove(media)
            } catch (_: Exception) {
                throw MediaNotFoundException(mediaId)
            }
            try {
                albumRepository.save(album).toDto()
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException()
            }
        } else
            throw MediaNotFoundException(mediaId)
    }
}