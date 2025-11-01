package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.TripAlreadyExistsException
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.service.TripCleanupService
import traversium.tripservice.service.TripService

@RestController
@RequestMapping("/rest/v1/trips")
class TripController(
    private val tripService: TripService,
    private val tripCleanupService: TripCleanupService
) : Logging {

    @GetMapping
    @Operation(
        summary = "Get all trips.",
        description = "Get all trips.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "All trips retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trips found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun getAllTrips(): ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getAllTrips()
            logger.info("Trips found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException){
            logger.error("No trips found.")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{tripId}")
    @Operation(
        summary = "Get trip by tripId.",
        description = "Get trip by tripId.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip by tripId retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
        ApiResponse(
            responseCode = "404",
            description = "No trip by this tripId found.",
        ),
        ApiResponse(
            responseCode = "500",
            description = "Internal server error.",
        )
        ]
    )
    fun getByTripId(
        @PathVariable tripId: Long
    ): ResponseEntity<TripDto> {
        return try{
            val trip = tripService.getByTripId(tripId)
            logger.info("Trip $tripId found.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException){
            logger.info("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(
        summary = "Get trip by ownerId.",
        description = "Get trip by ownerId.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip by ownerId retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trips by this ownerId found.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - invalid ownerId provided.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun getTripsByOwner(
        @PathVariable ownerId: String
    ): ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getTripsByOwner(ownerId)
            logger.info("Trips by owner $ownerId found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException){
            logger.info("No trips by owner $ownerId found.")
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @Operation(
        summary = "Create new trip.",
        description = "Create new trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "New trip successfully created.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid trip data provided.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - trip already exists.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun createTrip(
        @RequestBody tripDto: TripDto
    ): ResponseEntity<TripDto> {
        return try {
            val trip = tripService.createTrip(tripDto)
            logger.info("Trip ${trip.tripId} created.")
            ResponseEntity.ok(trip)
        } catch (_: TripAlreadyExistsException) {
            logger.info("Trip ${tripDto.tripId} already exists.")
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (_: Exception){
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping()
    @Operation(
        summary = "Update trip by tripId.",
        description = "Update trip by tripId.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully updated.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Trip not found.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - invalid trip data provided.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun updateTrip(
        @RequestBody tripDto: TripDto
    ): ResponseEntity<TripDto> {
        return try {
            val trip = tripService.updateTrip( tripDto)
            logger.info("Trip ${trip.tripId} updated.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException) {
            logger.info("Trip ${tripDto.tripId} not found.")
            ResponseEntity.notFound().build()
        } catch (_: Exception){
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{tripId}")
    @Operation(
        summary = "Delete trip by tripId.",
        description = "Delete trip by tripId.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully deleted.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trips by this tripId found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun deleteTrip(
        @PathVariable tripId: Long
    ): ResponseEntity<Void> {
        return try {
            tripService.deleteTrip(tripId)
            logger.info("Trip $tripId deleted.")
            ResponseEntity.ok().build()
        } catch (_: TripNotFoundException){
            logger.info("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/collaborators/{collaboratorId}")
    @Operation(
        summary = "Get trip by collaboratorId.",
        description = "Get trip by collaboratorId.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip by collaboratorId retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trips by this user found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun getTripsByCollaborator(
        @PathVariable collaboratorId: String
    ) : ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getTripsByCollaborator(collaboratorId)
            logger.info("Trips by user $collaboratorId found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException) {
            logger.info("No trips by user $collaboratorId found.")
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{tripId}/collaborators/{collaboratorId}")
    @Operation(
        summary = "Add collaborator to a trip.",
        description = "Add collaborator to a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully added.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun addCollaboratorToTrip(
        @PathVariable tripId: Long,
        @PathVariable collaboratorId: String
    ) : ResponseEntity<TripDto> {
        return try {
            val trip = tripService.addCollaboratorToTrip(tripId, collaboratorId)
            logger.info("Collaborator $collaboratorId successfully added to trip $tripId.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException) {
            logger.info("")
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{tripId}/collaborators/{collaboratorId}")
    @Operation(
        summary = "Remove collaborator from trip.",
        description = "Remove collaborator from a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully removed.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip found.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - no collaborator found",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun deleteCollaboratorFromTrip(
        @PathVariable tripId: Long,
        @PathVariable collaboratorId: String
    ) : ResponseEntity<Void> {
        return try {
            tripService.deleteCollaboratorFromTrip(tripId, collaboratorId)
            ResponseEntity.ok().build()
        } catch (_: TripNotFoundException) {
            logger.info("")
            ResponseEntity.notFound().build()
        } catch (_: Exception) {
            logger.info("")
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/viewers/{viewerId}")
    @Operation(
        summary = "Get all trips for viewer ID.",
        description = "Get all trips for viewer ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Viewer successfully retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trips found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun getTripsByViewer(
        @PathVariable viewerId: String,
    ) : ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getTripsByViewer(viewerId)
            logger.info("Trips by viewer $viewerId found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException) {
            logger.info("No trips by viewer $viewerId found.")
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{tripId}/viewers/{viewerId}")
    @Operation(
        summary = "Add viewer to a trip.",
        description = "Add viewer to a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully added.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun addViewerToTrip(
        @PathVariable tripId: Long,
        @PathVariable viewerId: String
    ) : ResponseEntity<TripDto> {
        return try {
            val trip = tripService.addViewerToTrip(tripId, viewerId)
            logger.info("Viewer $viewerId successfully added to trip $tripId.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException) {
            logger.info("")
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{tripId}/viewers/{viewerId}")
    @Operation(
        summary = "Remove viewer from a trip.",
        description = "Remove viewer from a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Viewer successfully removed from trip.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun deleteViewerToTrip(
        @PathVariable tripId: Long,
        @PathVariable viewerId: String,
    ) : ResponseEntity<Void> {
        return try {
            tripService.deleteViewerFromTrip(tripId, viewerId)
            logger.info("Viewer $viewerId successfully removed from trip $tripId.")
            ResponseEntity.ok().build()
        } catch (_: TripNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{tripId}/albums/{albumId}")
    @Operation(
        summary = "Get album by albumId from trip.",
        description = "Get album by albumId from trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album by albumId retrieved from trip.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No albums found in this trip.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun getAlbumFromTrip(
        @PathVariable tripId: Long,
        @PathVariable albumId: Long
    ) : ResponseEntity<AlbumDto> {
        return try {
            val trip = tripService.getAlbumFromTrip(tripId, albumId)
            logger.info("Album $albumId retrieved from trip $trip.")
            ResponseEntity.ok(trip)
        } catch (_: AlbumNotFoundException) {
            logger.info("No albums in trip $tripId found.")
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{tripId}/albums")
    @Operation(
        summary = "Add album to trip.",
        description = "Add album to trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album successfully added to trip.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip found",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - invalid album data provided",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
            )
        ]
    )
    fun addAlbumToTrip(
        @PathVariable tripId: Long,
        dto: AlbumDto) : ResponseEntity<TripDto> {
        return try {
            val trip = tripService.addAlbumToTrip(tripId, dto)
            logger.info("Album ${dto.albumId} added to trip $tripId.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException) {
            logger.info("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{tripId}/albums/{albumId}")
    @Operation(
        summary = "Delete album from trip by tripId.",
        description = "Delete album from trip by tripId.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album successfully deleted from trip.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip found.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - no album for this id found.",
            )
        ]
    )
    fun deleteAlbumFromTrip(
        @PathVariable tripId: Long,
        @PathVariable albumId: Long
    ): ResponseEntity<Void> {
        return try {
            tripService.deleteAlbumFromTrip(tripId, albumId)
            ResponseEntity.ok().build()
        } catch (_: TripNotFoundException){
            ResponseEntity.notFound().build()
        } catch (_: Exception){
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/block/{blockerId}/{blockedId}")
    @Operation(
        summary = "Remove blocked user relations from trips.",
        description = "Removes all viewer and collaborator connections between a blocker and a blocked user. " +
                "Used when one user blocks another across all trips.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "User relations successfully removed."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request - missing or invalid user IDs."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error."
            )
        ]
    )
    fun removeBlockedUserRelations(
        @PathVariable blockerId: String,
        @PathVariable blockedId: String
    ): ResponseEntity<String> {
        return try {
            tripCleanupService.removeBlockedUserRelations(blockerId, blockedId)
            logger.info("Removed blocked user relations between $blockerId and $blockedId")
            ResponseEntity.ok("Relations between $blockerId and $blockedId successfully removed.")
        } catch (e: Exception) {
            logger.error("Failed to remove blocked user relations between $blockerId and $blockedId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to remove user relations: ${e.message}")
        }
    }

}
