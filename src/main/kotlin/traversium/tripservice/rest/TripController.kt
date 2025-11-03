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
import traversium.tripservice.exceptions.TripHasCollaboratorException
import traversium.tripservice.exceptions.TripHasViewerException
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.exceptions.TripWithoutCollaboratorException
import traversium.tripservice.exceptions.TripWithoutViewerException
import traversium.tripservice.service.TripService

@RestController
@RequestMapping("/rest/v1/trips")
class TripController(
    private val tripService: TripService,
) : Logging {

    @GetMapping
    @Operation(
        summary = "Get all trips",
        description = "Returns all trips.",
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
        summary = "Get trip",
        description = "Returns trip by ID.",
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
        summary = "Get trips by owner",
        description = "Returns all trips by owner with ID.",
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
        summary = "Create new trip",
        description = "Creates a new trip.",
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

    @PutMapping
    @Operation(
        summary = "Update trip",
        description = "Updates trip by ID.",
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
        summary = "Delete trip",
        description = "Deletes trip by ID. Note: Also deletes its albums and media!",
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
        summary = "Get trips by collaborator",
        description = "Returns all trips by collaborator ID.",
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
        summary = "Add collaborator to trip",
        description = "Adds a collaborator to a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Collaborator successfully added.",
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
                responseCode = "409",
                description = "Conflict - collaborator already exists in trip."
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
            logger.info("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripHasCollaboratorException) {
            logger.info("Collaborator with ID $collaboratorId already exists in trip $tripId.")
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @DeleteMapping("/{tripId}/collaborators/{collaboratorId}")
    @Operation(
        summary = "Remove collaborator from trip",
        description = "Removes a collaborator from trip by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Collaborator successfully removed.",
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
            logger.info("No trip $tripId found.")
            ResponseEntity.notFound().build()
        } catch (_: TripWithoutCollaboratorException) {
            logger.info("No collaborator $collaboratorId in trip $tripId found.")
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/viewers/{viewerId}")
    @Operation(
        summary = "Get trips by viewer",
        description = "Gets all trips by viewer ID.",
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
                responseCode = "409",
                description = "",
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
        summary = "Add viewer to trip",
        description = "Adds a viewer to trip by ID.",
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
                responseCode = "409",
                description = "Conflict - viewer already exists",
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
            logger.info("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripHasViewerException) {
            logger.info("Viewer with ID $viewerId already exists in trip $tripId.")
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @DeleteMapping("/{tripId}/viewers/{viewerId}")
    @Operation(
        summary = "Remove viewer from trip",
        description = "Removes a viewer from trip by ID.",
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
                responseCode = "409",
                description = "Bad request - no viewer found.",
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
            logger.info("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripWithoutViewerException) {
            logger.info("No viewer $viewerId found in trip $tripId.")
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{tripId}/albums/{albumId}")
    @Operation(
        summary = "Get album from trip",
        description = "Gets an album by ID from trip by ID.",
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
        summary = "Add album to trip",
        description = "Adds an album to trip by ID.",
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
        summary = "Delete album from trip",
        description = "Deletes an album from trip by ID. Note: Also deletes its media! ",
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

}
