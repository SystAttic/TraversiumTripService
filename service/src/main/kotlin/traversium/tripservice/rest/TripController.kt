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
import traversium.tripservice.exceptions.AlbumModerationException
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.AlbumUnauthorizedException
import traversium.tripservice.exceptions.TripAlreadyExistsException
import traversium.tripservice.exceptions.TripHasCollaboratorException
import traversium.tripservice.exceptions.TripHasViewerException
import traversium.tripservice.exceptions.TripModerationException
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.exceptions.TripUnauthorizedException
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
    fun getAllTrips(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getAllTrips(offset, limit)
            logger.info("Trips found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException){
            logger.warn("No trips found.")
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to get trip."
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
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (e: TripUnauthorizedException){
            logger.warn(e.message.toString())
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to get trips."
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
    fun getTripsByOwner(
        @PathVariable ownerId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getTripsByOwner(ownerId, offset, limit)
            logger.info("Trips by owner $ownerId found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException){
            logger.warn("No trips by owner $ownerId found.")
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to create trip."
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
            logger.warn("Trip ${tripDto.tripId} already exists.")
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (_: TripHasViewerException) {
            logger.warn("Trip already has a viewer.")
            ResponseEntity.badRequest().body(tripDto)
        } catch (_: TripHasCollaboratorException) {
            logger.warn("Trip already has a collaborator.")
            ResponseEntity.badRequest().build()
        } catch (e: TripModerationException){
            logger.warn("Trip moderation failed: ${e.message}")
            when (e.cause) {
                null -> ResponseEntity.unprocessableEntity().build()
                else -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
            }
        } catch (e: Exception){
            logger.warn(e.message.toString())
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to update trip."
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
            val trip = tripService.updateTrip(tripDto)
            logger.info("Trip ${trip.tripId} updated.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException) {
            logger.warn("Trip ${tripDto.tripId} not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripUnauthorizedException) {
            logger.warn("User is not authorized to update trip.")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: TripModerationException){
            logger.warn("Trip moderation failed: ${e.message}")
            when (e.cause) {
                null -> ResponseEntity.unprocessableEntity().build()
                else -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
            }
        } catch (e: Exception){
            logger.warn(e.message.toString())
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
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        }
    }

    /*
    *   <-- Collaborators -->
    */

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
                responseCode = "409",
                description = "Bad request - invalid collaborator data",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun getTripsByCollaborator(
        @PathVariable collaboratorId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ) : ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getTripsByCollaborator(collaboratorId, offset, limit)
            logger.info("Trips by collaborator $collaboratorId found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException) {
            logger.warn("No trips by collaborator $collaboratorId found.")
            ResponseEntity.notFound().build()
        } catch (e: Exception){
            logger.warn(e.message.toString())
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{tripId}/collaborators/{collaboratorId}")
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
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripHasCollaboratorException) {
            logger.warn("Collaborator with ID $collaboratorId already exists in trip $tripId.")
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
            logger.warn("No trip $tripId found.")
            ResponseEntity.notFound().build()
        } catch (_: TripWithoutCollaboratorException) {
            logger.warn("No collaborator $collaboratorId in trip $tripId found.")
            ResponseEntity.badRequest().build()
        } catch (e: Exception){
            logger.warn(e.message.toString())
            ResponseEntity.badRequest().build()
        }
    }

    /*
    *   <-- Viewers -->
    */

    @GetMapping("/viewers")
    @Operation(
        summary = "Get viewed trips",
        description = "Gets all viewed trips.",
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
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ) : ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.getTripsByViewer(offset, limit)
            logger.info("Viewed trips found.")
            ResponseEntity.ok(trips)
        } catch (_: TripNotFoundException) {
            logger.warn("No viewed trips found.")
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{tripId}/viewers/{viewerId}")
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
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripHasViewerException) {
            logger.warn("Viewer with ID $viewerId already exists in trip $tripId.")
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
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (_: TripWithoutViewerException) {
            logger.warn("No viewer $viewerId found in trip $tripId.")
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
            logger.warn("No albums in trip $tripId found.")
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
        @RequestBody dto: AlbumDto) : ResponseEntity<TripDto> {
        return try {
            val trip = tripService.addAlbumToTrip(tripId, dto)
            logger.info("Album ${dto.title} added to trip $tripId.")
            ResponseEntity.ok(trip)
        } catch (_: TripNotFoundException) {
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (e: AlbumModerationException){
            logger.warn("Album moderation failed: ${e.message}")
            when (e.cause) {
                null -> ResponseEntity.unprocessableEntity().build()
                else -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
            }
        } catch (_: Exception) {
            logger.warn("Invalid album data provided.")
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to delete album.",
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
            logger.info("Album $albumId deleted from trip $tripId.")
            ResponseEntity.ok().build()
        } catch (_: TripNotFoundException) {
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        } catch (e: AlbumUnauthorizedException) {
            logger.warn("User is not authorized to delete album $albumId: ${e.message}")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: TripUnauthorizedException) {
            logger.warn("User is not authorized to delete album $albumId: ${e.message}")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (_: Exception){
            logger.warn("Album $albumId for trip $tripId not found.")
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{tripId}/media")
    @Operation(
        summary = "Get all media from trip",
        description = "Gets all media from trip by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media successfully retrieved from trip.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trip by this ID found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
            )
        ]
    )
    fun getAllMediaFromTrip(
        @PathVariable tripId: Long,
    ) : ResponseEntity<List<String>> {
        return try {
            val allMedia = tripService.getAllMediaFromTrip(tripId)
            logger.info("All media retrieved from trip $tripId.")
            ResponseEntity.ok(allMedia)
        } catch (_: TripNotFoundException) {
            logger.warn("Trip $tripId not found.")
            ResponseEntity.notFound().build()
        }

    }

    @GetMapping("/search")
    @Operation(
        summary = "Search trips by title",
        description = "Search trips by title with partial matching and pagination.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trips found.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid query parameter."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error."
            )
        ]
    )
    fun searchTripsByTitle(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TripDto>> {
        return try {
            val trips = tripService.searchTripsByTitle(query, offset, limit)
            logger.info("Found ${trips.size} trips matching query '$query'")
            ResponseEntity.ok(trips)
        } catch (e: Exception) {
            logger.warn("Error searching trips: ${e.message}")
            ResponseEntity.badRequest().build()
        }
    }

}
