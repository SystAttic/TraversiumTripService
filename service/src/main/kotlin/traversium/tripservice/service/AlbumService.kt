package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.audit.kafka.ActivityType
import traversium.audit.kafka.AuditStreamData
import traversium.audit.kafka.EntityType
import traversium.audit.kafka.TripActivityAction
import traversium.notification.kafka.ActionType
import traversium.notification.kafka.NotificationStreamData
import traversium.tripservice.db.model.Media
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.*
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.DomainEvent
import traversium.tripservice.kafka.data.MediaEvent
import traversium.tripservice.kafka.data.MediaEventType
import traversium.tripservice.kafka.data.ReportingStreamData
import java.time.OffsetDateTime
import java.time.YearMonth

@Service
@Transactional
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val tripRepository: TripRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val firebaseService: FirebaseService,
    private val tripService: TripService,
    private val moderationServiceGrpcClient: ModerationServiceGrpcClient
) {
    private fun <T : DomainEvent> publishEvent(event: T) {
        val wrapped = ReportingStreamData(
            timestamp = YearMonth.now(),
            action = event
        )
        eventPublisher.publishEvent(wrapped)
    }

    private fun publishNotification(action: ActionType, sender: String, collaborators: List<String>, trip: Long?, album: Long?, mediaCount: Int?) {
        val event = NotificationStreamData(
            senderId = sender,
            receiverIds = collaborators,
            action = action,
            timestamp = OffsetDateTime.now(),
            collectionReferenceId = trip,
            nodeReferenceId = album,
            commentReferenceId = null,
            mediaReferenceId = null,
            mediaCount = mediaCount
        )

        eventPublisher.publishEvent(event)
    }

    private fun publishAuditEvent(firebaseId: String, action: String, entityType: EntityType, entityId: Long, tripId: Long, vararg metadata: Pair<String, String>) {
        val auditEvent = AuditStreamData(
            timestamp = OffsetDateTime.now(),
            userId = firebaseId,
            activityType = ActivityType.TRIP_ACTIVITY,
            action = action,
            entityType = entityType,
            entityId = entityId,
            tripId = tripId,
            metadata = mapOf(
                *metadata,
                "tripId" to tripId,
                "entityType" to entityType,
                "action" to action
            )
        )

        eventPublisher.publishEvent(auditEvent)
    }

    private fun getFirebaseIdFromContext(): String =
        firebaseService.extractUidFromToken(SecurityContextHolder.getContext().authentication.credentials as String)

    private fun getTripIdByAlbumId(albumId: Long): Long {
        return tripRepository.findTripIdByAlbumId(albumId)
            .orElseThrow { NotFoundException("Trip with album $albumId not found.") }
    }

    private fun authorizeView(tripId: Long, firebaseId: String) {
        val trip = tripRepository.findById(tripId).orElseThrow { NotFoundException("Trip $tripId not found.") }

        // Use the same authorization logic as TripService's isUserAuthorizedToView
        val isAuthorized = trip.visibility == Visibility.PUBLIC ||
                trip.collaborators.contains(firebaseId) ||
                trip.viewers.contains(firebaseId) ||
                trip.ownerId == firebaseId

        if (!isAuthorized) {
            throw UnauthorizedException("User is not authorized to view this album.")
        }
    }

    private fun authorizeModify(tripId: Long, firebaseId: String) {
        val trip = tripRepository.findById(tripId).orElseThrow { NotFoundException("Trip $tripId not found.") }

        val isAuthorized = trip.collaborators.contains(firebaseId) || trip.ownerId == firebaseId

        if (!isAuthorized) {
            throw UnauthorizedException("User is not authorized to modify this album.")
        }
    }


    fun getAllAlbums(): List<AlbumDto> {
        val firebaseId = getFirebaseIdFromContext()
        // This repository method is already assumed to handle security filtering.
        val albums = albumRepository.findAllAccessibleAlbumsByUserId(firebaseId)

        if(albums.isEmpty())
            throw NotFoundException("No albums found.")

        return albums.map { it.toDto() }
    }

    fun getAllAlbums(offset: Int, limit: Int): List<AlbumDto> {
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)
        val albums = albumRepository.findAllAccessibleAlbumsByUserId(firebaseId, pageable)

        if(albums.isEmpty())
            throw NotFoundException("No albums found.")

        return albums.map { it.toDto() }
    }

    fun getByAlbumId(albumId: Long): AlbumDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeView(tripId, firebaseId)

        return albumRepository.findById(albumId)
            .orElseThrow { NotFoundException("Album $albumId not found.") }
            .toDto()
    }

    @Transactional
    fun updateAlbum(albumId: Long, dto: AlbumDto): AlbumDto {

        // Moderate Trip text fields
        val allowed = try {
            val textToModerate = buildString {
                append(dto.title)
                append(" ")
                append(dto.description)
            }
            moderationServiceGrpcClient.isTextAllowed(textToModerate)
        } catch (e: Exception) {
            throw ModerationException("Moderation service unavailable",e)
        }
        // Block Trip creation if text not allowed
        if (!allowed) {
            throw ModerationException("Album content violates moderation policy!")
        }

        //If Moderation passed, continue.
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeModify(tripId, firebaseId)

        val trip = tripService.getByTripId(tripId)

        val existingAlbum = albumRepository.findById(albumId)
            .orElseThrow { NotFoundException("Album $albumId not found.") }

        val updatedAlbum = existingAlbum.copy(
            title = dto.title ?: existingAlbum.title,
            description = dto.description ?: existingAlbum.description,
        )

        // Kafka event - Album UPDATE
        publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_UPDATED,
                albumId = updatedAlbum.albumId,
                title = updatedAlbum.title,
            )
        )

        // Notification - Title change
        if (dto.title != null && dto.title != existingAlbum.title) {
            publishNotification(
                ActionType.CHANGE_TITLE,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                existingAlbum.albumId,
                null
            )

            // Audit - Album TITLE CHANGE
            publishAuditEvent(firebaseId, TripActivityAction.ALBUM_TITLE_CHANGED.name, EntityType.ALBUM, existingAlbum.albumId!!, trip.tripId!!)

        }

        // Notification - Description change
        if (dto.description != null && dto.description != existingAlbum.description) {
            publishNotification(
                ActionType.CHANGE_DESCRIPTION,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                existingAlbum.albumId,
                null
            )

            // Audit - Album DESCRIPTION CHANGE
            publishAuditEvent(firebaseId, TripActivityAction.ALBUM_DESCRIPTION_CHANGED.name, EntityType.ALBUM, existingAlbum.albumId!!, trip.tripId!!)

        }

        // Audit - Album INFO CHANGE
        publishAuditEvent(firebaseId, TripActivityAction.ALBUM_INFO_CHANGED.name, EntityType.ALBUM, existingAlbum.albumId!!,trip.tripId!!)

        return try {
            albumRepository.save(updatedAlbum).toDto()
        } catch (e: Exception) {
            throw DatabaseException("Error updating album $albumId.")
        }
    }

    fun getMediaFromAlbum(albumId: Long, mediaId: Long): MediaDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeView(tripId, firebaseId)

        val album = albumRepository.findById(albumId).orElseThrow { NotFoundException("Album $albumId not found.") }

        if(album.media.isEmpty())
            throw NotFoundException("No media in album $albumId.")
        else {
            return try {
                album.media.first { it.mediaId == mediaId }.toDto()
            } catch (_: NoSuchElementException) {
                throw NotFoundException("No media $mediaId in album $albumId.")
            }
        }
    }

    data class FailedMediaItem(
        val pathUrl: String, // The pathUrl of the media that failed
        val reason: String // The reason for the failure
    )

    @Transactional
    fun addMediaToAlbum(albumId: Long, dtos: List<MediaDto>) : AlbumDto {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeModify(tripId, firebaseId)

        val album = albumRepository.findById(albumId).orElseThrow{ NotFoundException("Album $albumId not found.") }
        val trip = tripRepository.findById(tripId).orElseThrow {
            NotFoundException("Trip $tripId not found.")
        }

        val successfulMedia = mutableListOf<Media>()
        val failedMedia = mutableListOf<FailedMediaItem>()
        val mediaToSave = mutableListOf<Media>()

        dtos.forEach {
            dto ->
                try{
                    val media = dto.copy(uploader = firebaseId).toMedia()
                    mediaToSave.add(media)
                } catch (e: Exception) {
                    failedMedia.add(FailedMediaItem(dto.pathUrl ?: "", "Media DTO conversion failed: ${e.message}"))
                }
        }

        // Exit early if no media items are valid
        if (mediaToSave.isEmpty() && failedMedia.isNotEmpty()) {
            return album.toDto()
        }

        // Add all media from mediaToSave to album
        try {
            album.media.addAll(mediaToSave)
        } catch (e: Exception) {
            throw DatabaseException("Failed to save media to album: ${e.message}")
        }

        // Save the album
        val savedAlbum = try {
            albumRepository.save(album)
        } catch (e: Exception) {
            throw DatabaseException("Failed to save album with batch media: ${e.message}")
        }

        // Map savedMedia to send out Kafka Reports and Kafka Audits for each
        val savedMediaMap = savedAlbum.media
            .filter { it.uploader == firebaseId && mediaToSave.any { m -> m.pathUrl == it.pathUrl } }
            .associateBy { it.pathUrl }

        mediaToSave.forEach { media ->
            val savedMedia = savedMediaMap[media.pathUrl]
            if (savedMedia != null && savedMedia.mediaId != null) {
                successfulMedia.add(savedMedia)

                // Publish events only for successfully saved media
                publishEvent(
                    MediaEvent(
                        eventType = MediaEventType.MEDIA_ADDED,
                        mediaId = savedMedia.mediaId,
                        pathUrl = savedMedia.pathUrl,
                    )
                )
                // Audit
                publishAuditEvent(firebaseId, TripActivityAction.MEDIA_UPLOADED.name, EntityType.PHOTO, savedMedia.mediaId!!, trip.tripId!!)
            } else {
                // This is unlikely if the save succeeded but acts as a safety net
                failedMedia.add(FailedMediaItem(media.pathUrl ?: "", "Media ID generation failed after successful transaction."))
            }
        }

        // Notification (only one call after the batch)
        if (successfulMedia.isNotEmpty()) {
            val mediaIds = successfulMedia.mapNotNull { it.mediaId }
            // For now, calling it once, uses the first Media ID in batch.
            // TODO: adapt API for batch notification -> change to notify the number of added Media
            publishNotification(
                ActionType.ADD,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                savedAlbum.albumId,
                mediaIds.count(),
            )
        }
        return savedAlbum.toDto()
    }

    @Transactional
    fun deleteMediaFromAlbum(albumId: Long, mediaId: Long) {
        val firebaseId = getFirebaseIdFromContext()
        val tripId = getTripIdByAlbumId(albumId)
        authorizeModify(tripId, firebaseId)

        val album = albumRepository.findById(albumId).orElseThrow { NotFoundException("Album $albumId not found.") }
        val trip = tripRepository.findById(tripId).orElseThrow { NotFoundException("Trip $tripId not found.") }

        if(album.media.any { it.mediaId == mediaId }) {
            val media = album.media.find { it.mediaId == mediaId }
            if (media == null) {
                throw NotFoundException("Media $mediaId not found in album $albumId.")
            }

            // Delete media
            try {
                album.media.remove(media)
            } catch (_: Exception) {
                throw DatabaseException("Media $mediaId could not be removed.")
            }

            // Save updated album
            try {
                albumRepository.save(album).toDto()
            } catch (_: Exception) {
                throw DatabaseException("Failed to delete media from album $albumId.")
            }

            // Kafka event - Media DELETE
            publishEvent(
                MediaEvent(
                    eventType = MediaEventType.MEDIA_DELETED,
                    mediaId = media.mediaId,
                    pathUrl = media.pathUrl,
                )
            )

            // Notification - Media DELETE
            publishNotification(
                ActionType.DELETE,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                album.albumId,
                1
            )

            // TODO - PHOTO->MEDIA in Auditor EntityType
            // Audit - Media DELETED
            publishAuditEvent(firebaseId, TripActivityAction.MEDIA_DELETED.name, EntityType.PHOTO, media.mediaId!!,trip.tripId!!)

        } else
            throw NotFoundException("Media $mediaId not found.")
    }
}