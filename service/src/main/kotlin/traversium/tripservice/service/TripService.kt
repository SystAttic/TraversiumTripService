package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.notification.kafka.ActionType
import traversium.notification.kafka.NotificationStreamData
import traversium.tripservice.db.model.Trip
import traversium.tripservice.db.model.Album
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.*
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.DomainEvent
import traversium.tripservice.kafka.data.ReportingStreamData
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.TripEventType
import traversium.audit.kafka.ActivityType
import traversium.audit.kafka.AuditStreamData
import traversium.audit.kafka.EntityType
import traversium.audit.kafka.TripActivityAction
import java.time.OffsetDateTime
import java.time.YearMonth
import java.util.UUID

@Service
@Transactional
class TripService(
    private val tripRepository: TripRepository,
    private val albumRepository: AlbumRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val firebaseService: FirebaseService
) {
    private fun <T : DomainEvent> publishEvent(event: T) {
        val wrapped = ReportingStreamData(
            timestamp = YearMonth.now(),
            action = event
        )
        eventPublisher.publishEvent(wrapped)
    }

    private fun publishNotification(action: ActionType, sender: String, collaborators: List<String>, trip: Long?, album: Long?) {
        val event = NotificationStreamData(
            senderId = sender,
            receiverIds = collaborators,
            action = action,
            timestamp = OffsetDateTime.now(),
            collectionReferenceId = trip,
            nodeReferenceId = album,
            commentReferenceId = null,
            mediaReferenceId = null
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

    private fun validateCollaborator(trip: Trip, collaboratorId: String) {
        if (trip.collaborators.contains(collaboratorId)) {
            throw TripHasCollaboratorException(collaboratorId)
        }
    }

    private fun validateViewer(trip: Trip, viewerId: String) {
        if (trip.viewers.contains(viewerId)) {
            throw TripHasViewerException(viewerId)
        }
    }

    private fun isUserAuthorizedToView(trip: Trip, userId: String): Boolean {
        // Check if the trip is PUBLIC, or if the user is a collaborator, or a viewer
        return tripRepository.isUserAuthorizedToView(trip.tripId!!, userId)

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
        if(trips.isEmpty())
            return emptyList()

        return trips.map { it.toDto() }
    }

    // where user is owner, viewer or collaborator (paginated)
    fun getAllTrips(offset: Int, limit: Int): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)
        val trips = tripRepository.findAllAccessibleTripsByUserId(firebaseId, pageable)
        if(trips.isEmpty())
            return emptyList()

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

    // trips where user is owner (paginated)
    fun getTripsByOwner(ownerId: String, offset: Int, limit: Int): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)
        return if (ownerId == firebaseId)
            tripRepository.findByOwnerId(ownerId, pageable).map { it.toDto() }
        else {
            // For other users' trips, we need to filter by visibility, but pagination is handled in memory
            // since the repository method doesn't support pagination for this case
            val allTrips = tripRepository.findByOwnerId(ownerId, firebaseId)
            val start = offset.coerceAtMost(allTrips.size)
            val end = (offset + limit).coerceAtMost(allTrips.size)
            allTrips.subList(start, end).map { it.toDto() }
        }
    }

    @Transactional
    fun createTrip(dto: TripDto): TripDto {
        if (
            //dto.ownerId == null ||
            dto.title == null || dto.tripId != null) {
            throw IllegalArgumentException("Trip title cannot be null.")
        }

        val firebaseId = getFirebaseIdFromContext()

        // DEFAULT ALBUM
        val defaultAlbum = Album(
            title = "Default moment",
            description = ""
        )

        try {
            albumRepository.save(defaultAlbum)
        } catch (_: Exception) {
            throw IllegalArgumentException()
        }

        val trip = dto.toTrip().copy(
            ownerId = firebaseId,
            defaultAlbum = defaultAlbum.albumId,
            albums = mutableListOf(defaultAlbum),
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

        val saved = try {
            tripRepository.save(trip)
        } catch (_: Exception) {
            throw IllegalArgumentException()
        }

        // Kafka event - Trip CREATE
        publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_CREATED,
                tripId = saved.tripId,
                ownerId = firebaseId,
            )
        )
        // Notification - Trip CREATE
        publishNotification(
            ActionType.CREATE,
            firebaseId,
            saved.collaborators,
            saved.tripId,
            null,
        )

        // Audit - Trip CREATE
        publishAuditEvent(firebaseId, TripActivityAction.TRIP_CREATED.name, EntityType.TRIP, saved.tripId!!,saved.tripId!!)

        return saved.toDto()
    }

    @Transactional
    fun deleteTrip(tripId: Long) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()

        if (firebaseId != trip.ownerId)
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        // Kafka event - Trip DELETE
        publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_DELETED,
                tripId = trip.tripId,
                ownerId = trip.ownerId,
            )
        )
        // Notification - Trip DELETE
        publishNotification(
            ActionType.DELETE,
            firebaseId,
            trip.collaborators,
            trip.tripId,
            null,
        )

        // Audit - Trip DELETE
        publishAuditEvent(firebaseId, TripActivityAction.TRIP_DELETED.name, EntityType.TRIP, trip.tripId!!,trip.tripId!!)

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
        publishEvent(
            TripEvent(
                eventType = TripEventType.TRIP_UPDATED,
                tripId = mergedTrip.tripId,
                ownerId = mergedTrip.ownerId,
            )
        )

        // Notification - Title change
        if (updated.title != null && updated.title != existingTrip.title) {
            publishNotification(
                ActionType.CHANGE_TITLE,
                firebaseId,
                mergedTrip.collaborators,
                mergedTrip.tripId,
                null,
            )

            // Audit - Trip TITLE CHANGE
            publishAuditEvent(firebaseId, TripActivityAction.TRIP_NAME_CHANGED.name, EntityType.TRIP, mergedTrip.tripId!!, mergedTrip.tripId!!)

        }

        // Notification - Description change
        if (updated.description != null && updated.description != existingTrip.description) {
            publishNotification(
                ActionType.CHANGE_DESCRIPTION,
                firebaseId,
                mergedTrip.collaborators,
                mergedTrip.tripId,
                null,
            )

            // Audit - Trip DESCRIPTION CHANGE
            publishAuditEvent(firebaseId, TripActivityAction.TRIP_DESCRIPTION_CHANGED.name, EntityType.TRIP, mergedTrip.tripId!!, mergedTrip.tripId!!)

        }

        // Notification - Cover photo change
        if (updated.coverPhotoUrl != null && updated.coverPhotoUrl != existingTrip.coverPhotoUrl) {
            publishNotification(
                ActionType.CHANGE_COVER_PHOTO,
                firebaseId,
                mergedTrip.collaborators,
                mergedTrip.tripId,
                null,
            )

            // Audit - Trip COVER PHOTO CHANGE
            publishAuditEvent(firebaseId, TripActivityAction.TRIP_COVER_PHOTO_CHANGED.name, EntityType.TRIP, mergedTrip.tripId!!,mergedTrip.tripId!!)

        }

        // TODO - add CHANGE_VISIBILITY -> NotificationService (uncomment after adding)
//        // Notification - Visibility change
//        if (updated.visibility != null && updated.visibility != existingTrip.visibility) {
//            publishNotification(
//                ActionType.CHANGE_VISIBILITY,
//                firebaseId,
//                mergedTrip.collaborators,
//                mergedTrip.tripId,
//                null,
//            )
//
//            // Audit - Trip VISIBILITY CHANGE
//            publishAuditEvent(firebaseId, TripActivityAction.TRIP_VISIBILITY_CHANGED.name, EntityType.TRIP, mergedTrip.tripId!!, mergedTrip.tripId!!)
//        }

        // Audit - Trip INFO CHANGE
        publishAuditEvent(firebaseId, TripActivityAction.TRIP_INFO_CHANGED.name, EntityType.TRIP, mergedTrip.tripId!!,mergedTrip.tripId!!)

        return tripRepository.save(mergedTrip).toDto()
    }

    /*
    *   <-- Collaborators -->
    */
    fun getTripsByCollaborator(collaboratorId: String): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        val trips = if (collaboratorId == firebaseId)
            tripRepository.findByCollaboratorId(firebaseId).map { it.toDto() }
        else
            tripRepository.findByCollaboratorId(collaboratorId, firebaseId).map { it.toDto() }

        if (trips.isEmpty())
            throw TripNotFoundException(0)
        else
            return trips
    }

    fun getTripsByCollaborator(collaboratorId: String, offset: Int, limit: Int): List<TripDto> {
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)
        val trips = if (collaboratorId == firebaseId)
            tripRepository.findByCollaboratorId(firebaseId).map { it.toDto() }
        else
            tripRepository.findByCollaboratorId(collaboratorId, firebaseId, pageable).map { it.toDto() }

        if (trips.isEmpty())
            throw TripNotFoundException(0)
        else
            return trips
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
        publishEvent(
            TripEvent(
                eventType = TripEventType.COLLABORATOR_ADDED,
                tripId = saved.tripId,
                ownerId = saved.ownerId
            )
        )
        // Notification - Trip ADD_COLLABORATOR
        publishNotification(
            ActionType.ADD_COLLABORATOR,
            firebaseId,
            saved.collaborators,
            saved.tripId,
            null,
        )

        // TODO - for future: EntityType: COLLABORATOR
        // Audit - Trip COLLABORATOR INVITED
        publishAuditEvent(firebaseId, TripActivityAction.TRIP_COLLABORATOR_INVITED.name, EntityType.TRIP, saved.tripId!!,saved.tripId!!)

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
            publishEvent(
                TripEvent(
                    eventType = TripEventType.COLLABORATOR_DELETED,
                    tripId = trip.tripId,
                    ownerId = trip.ownerId,
                )
            )
            trip.collaborators.remove(collaboratorId)

            // Notification - Trip REMOVE_COLLABORATOR
            publishNotification(
                ActionType.REMOVE_COLLABORATOR,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                null,
            )

            // TODO - for future: EntityType: COLLABORATOR
            // Audit - Trip COLLABORATOR REMOVED
            publishAuditEvent(firebaseId, TripActivityAction.TRIP_COLLABORATOR_REMOVED.name, EntityType.TRIP, trip.tripId!!,trip.tripId!!)

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
        if (trips.isEmpty())
            throw TripNotFoundException(0)

        return trips.map { it.toDto() }
    }

    // get trips where user is a viewer (paginated)
    fun getTripsByViewer(offset: Int, limit: Int): List<TripDto>{
        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)
        val trips = tripRepository.findByViewerId(firebaseId, pageable)
        if (trips.isEmpty())
            throw TripNotFoundException(0)

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
        publishEvent(
            TripEvent(
                eventType = TripEventType.VIEWER_ADDED,
                tripId = saved.tripId,
                ownerId = saved.ownerId
            )
        )

        // Notification - Trip ADD_VIEWER
        publishNotification(
            ActionType.ADD_VIEWER,
            firebaseId,
            saved.collaborators,
            saved.tripId,
            null,
        )

        // TODO - for future: EntityType: VIEWER
        // Audit - Trip VIEWER INVITED
        publishAuditEvent(firebaseId, TripActivityAction.TRIP_VIEWER_INVITED.name, EntityType.TRIP, saved.tripId!!, saved.tripId!!)

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
            publishEvent(
                TripEvent(
                    eventType = TripEventType.VIEWER_DELETED,
                    tripId = trip.tripId,
                    ownerId = trip.ownerId,
                )
            )
            trip.viewers.remove(viewerId)

            // Notification - Trip REMOVE_VIEWER
            publishNotification(
                ActionType.REMOVE_VIEWER,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                null,
            )

            // TODO - for future: EntityType: VIEWER
            // Audit - Trip VIEWER REMOVED
            publishAuditEvent(firebaseId, TripActivityAction.TRIP_VIEWER_REMOVED.name, EntityType.TRIP, trip.tripId!!, trip.tripId!!)

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
        if (!trip.collaborators.contains(firebaseId))
            throw TripUnauthorizedException("User is not authorized to perform this operation.")

        val marker = UUID.randomUUID().toString()

        val newAlbum = dto.toAlbum().copy(
            description = marker
        )
        trip.albums.add(newAlbum)

        val savedTrip = tripRepository.save(trip)
        val albumWithId = savedTrip.albums.find { it.description == marker }

        requireNotNull(albumWithId) {"Failed to find the newly added album by unique marker."}
        val albumId = requireNotNull(albumWithId.albumId)

        albumWithId.description = dto.description ?: ""
        val savedTripDto = savedTrip.toDto()

        // Kafka event - Album CREATE
        publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_CREATED,
                albumId = albumId,
                title = newAlbum.title,
            )
        )

        // Notification - Album CREATE
        publishNotification(
            ActionType.CREATE,
            firebaseId,
            savedTripDto.collaborators,
            savedTripDto.tripId,
            albumId,
        )

        // Audit - Album CREATE
        publishAuditEvent(firebaseId, TripActivityAction.ALBUM_CREATED.name, EntityType.ALBUM, albumId,savedTripDto.tripId!!)


        return savedTripDto
    }

    @Transactional
    fun deleteAlbumFromTrip(tripId: Long, albumId: Long) {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }

        val firebaseId = getFirebaseIdFromContext()
        if (!trip.collaborators.contains(firebaseId)) {
            throw TripUnauthorizedException("User is not authorized to perform this operation.")
        }

        if (trip.defaultAlbum == albumId)
            throw AlbumUnauthorizedException("Cannot delete Default album.")

        if(trip.albums.any { it.albumId == albumId }) {
            val album = trip.albums.find { it.albumId == albumId }

            // Kafka event - Album DELETE
            publishEvent(
                AlbumEvent(
                    eventType = AlbumEventType.ALBUM_DELETED,
                    albumId = album!!.albumId,
                    title = album.title
                )
            )
            trip.albums.remove(album)

            // Notification - Album DELETE
            publishNotification(
                ActionType.DELETE,
                firebaseId,
                trip.collaborators,
                trip.tripId,
                albumId,
            )

            // Audit - Album DELETE
            publishAuditEvent(firebaseId, TripActivityAction.ALBUM_DELETED.name, EntityType.ALBUM, albumId, trip.tripId!!)


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

    // Search trips by title (partial match, case-insensitive)
    fun searchTripsByTitle(query: String, offset: Int, limit: Int): List<TripDto> {
        if (query.isBlank()) {
            return emptyList()
        }

        val firebaseId = getFirebaseIdFromContext()
        val pageable = PageRequest.of(offset / limit, limit)
        val trips = tripRepository.searchTripsByTitle(query, firebaseId, pageable)

        return trips.map { it.toDto() }
    }

}
