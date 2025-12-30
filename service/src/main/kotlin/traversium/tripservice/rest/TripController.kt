package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.ErrorResponse
import traversium.tripservice.dto.TripDto
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.SwaggerErrorObjects
import traversium.tripservice.exceptions.SwaggerRequestObjects
import traversium.tripservice.exceptions.SwaggerResponseObjects
import traversium.tripservice.service.TripService
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("/rest/v1/trips")
class TripController(
    private val tripService: TripService,
) : Logging {

    @GetMapping
    @Operation(
        operationId = "getAllTrips",
        tags = ["Trip"],
        summary = "Get all trips",
        description = "Returns all trips for a user.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trips successfully retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get all trips Success",
                            name = "Successfully retrieved all trips.",
                            description = "All successfully retrieved trips.",
                            value= SwaggerResponseObjects.GET_ALL_TRIPS
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - No trips found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getAllTrips(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TripDto>> {
            val trips = tripService.getAllTrips(offset, limit)
            logger.info("Trips found.")
            return ResponseEntity.ok(trips)
    }

    @GetMapping("/{tripId}")
    @Operation(
        operationId = "getTripById",
        tags = ["Trip"],
        summary = "Get trip",
        description = "Returns trip by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get trip by ID Success",
                            name = "Successfully retrieved trip by ID.",
                            description = "Successfully retrieved trip.",
                            value= SwaggerResponseObjects.GET_TRIP_BY_ID
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden – Unauthorized to access this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getByTripId(
        @PathVariable tripId: Long
    ): ResponseEntity<TripDto> {
        val trip = tripService.getByTripId(tripId)
        logger.info("Trip $tripId found.")
        return ResponseEntity.ok(trip)
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(
        operationId = "getTripsByOwner",
        tags = ["Trip"],
        summary = "Get trips by owner",
        description = "Returns all trips by owner.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trips successfully retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get trip by owner Success",
                            name = "Successfully retrieved trip by owner.",
                            description = "Successfully retrieved trip.",
                            value= SwaggerResponseObjects.GET_TRIP_BY_OWNER
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to get trips.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - No trips found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getTripsByOwner(
        @PathVariable ownerId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TripDto>> {
        val trips = tripService.getTripsByOwner(ownerId, offset, limit)
        logger.info("Trips by owner $ownerId found.")
        return ResponseEntity.ok(trips)
    }

    @PostMapping
    @Operation(
        operationId = "createTrip",
        tags = ["Trip"],
        summary = "Create new trip",
        description = "Creates a new trip.",
        requestBody = SwaggerRequestBody(
            description = "New trip information",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            name = "Example of a new trip.",
                            summary = "Create trip Example",
                            value = SwaggerRequestObjects.CREATE_TRIP
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "New trip successfully created.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Create Trip Success",
                            name = "Successfully created trip.",
                            description = "Returned trip after successful creation.",
                            value= SwaggerResponseObjects.CREATED_TRIP
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid trip data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to create trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Trip already exists.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.CONFLICT_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun createTrip(
        @RequestBody tripDto: TripDto
    ): ResponseEntity<TripDto> {
        val trip = tripService.createTrip(tripDto)
        logger.info("Trip ${trip.tripId} created.")
        return ResponseEntity.ok(trip)
    }

    @PutMapping
    @Operation(
        operationId = "updateTrip",
        tags = ["Trip"],
        summary = "Update trip",
        description = "Updates a trip.",
        requestBody = SwaggerRequestBody(
            description = "Updated trip information",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Update Trip Example",
                            name = "Example of an updated trip.",
                            value = SwaggerRequestObjects.UPDATE_TRIP
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully updated.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Update trip Success",
                            name = "Successfully updated trip.",
                            description = "Returned trip after successful update.",
                            value= SwaggerResponseObjects.UPDATED_TRIP
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid trip data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to update this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )
                ]
            )
        ]
    )
    fun updateTrip(
        @RequestBody tripDto: TripDto
    ): ResponseEntity<TripDto> {
        val trip = tripService.updateTrip(tripDto)
        logger.info("Trip ${tripDto.tripId} updated.")
        return ResponseEntity.ok(trip)
    }

    @DeleteMapping("/{tripId}")
    @Operation(
        operationId = "deleteTrip",
        tags = ["Trip"],
        summary = "Delete trip",
        description = "Deletes trip by ID and all its content.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trip successfully deleted.",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to delete this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun deleteTrip(
        @PathVariable tripId: Long
    ): ResponseEntity<Void> {
        tripService.deleteTrip(tripId)
        logger.info("Trip $tripId deleted.")
        return ResponseEntity.ok().build()
    }

    /*
    *   <-- Collaborators -->
    */

    @GetMapping("/collaborators/{collaboratorId}")
    @Operation(
        operationId = "getTripsByCollaborator",
        tags = ["Trip"],
        summary = "Get trips by collaborator",
        description = "Returns all trips by a collaborator.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trips successfullly retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get trip by collaborator Success",
                            name = "Successfully retrieved trips with collaborator.",
                            description = "Returned trips with collaborator.",
                            value= SwaggerResponseObjects.GET_TRIPS_BY_COLLABORATOR
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trips not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getTripsByCollaborator(
        @PathVariable collaboratorId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ) : ResponseEntity<List<TripDto>> {
        val trips = tripService.getTripsByCollaborator(collaboratorId, offset, limit)
        logger.info("Trips by collaborator $collaboratorId found.")
        return ResponseEntity.ok(trips)
    }

    @PutMapping("/{tripId}/collaborators/{collaboratorId}")
    @Operation(
        operationId = "addCollaboratorToTrip",
        tags = ["Collaborator"],
        summary = "Add collaborator to trip",
        description = "Adds a collaborator to a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Collaborator successfully added.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Add collaborator Success",
                            name = "Successfully added collaborator to trip.",
                            description = "Returned trip with added collaborator.",
                            value= SwaggerResponseObjects.ADDED_COLLABORATOR
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to add a collaborator to this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Collaborator already exists in this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.CONFLICT_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun addCollaboratorToTrip(
        @PathVariable tripId: Long,
        @PathVariable collaboratorId: String
    ) : ResponseEntity<TripDto> {
        val trip = tripService.addCollaboratorToTrip(tripId, collaboratorId)
        logger.info("Collaborator $collaboratorId successfully added to trip $tripId.")
        return ResponseEntity.ok(trip)
    }

    @DeleteMapping("/{tripId}/collaborators/{collaboratorId}")
    @Operation(
        operationId = "removeCollaboratorFromTrip",
        tags = ["Collaborator"],
        summary = "Remove collaborator from trip",
        description = "Removes a collaborator from a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Collaborator successfully removed."
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to remove a collaborator from this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Collaborator missing in trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.CONFLICT_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun removeCollaboratorFromTrip(
        @PathVariable tripId: Long,
        @PathVariable collaboratorId: String
    ) : ResponseEntity<Void> {
        tripService.removeCollaboratorFromTrip(tripId, collaboratorId)
        logger.info("Collaborator $collaboratorId successfully removed from trip $tripId.")
        return ResponseEntity.ok().build()
    }

    /*
    *   <-- Viewers -->
    */

    @GetMapping("/viewers")
    @Operation(
        operationId = "getTripsByViewer",
        tags = ["Trip"],
        summary = "Get viewed trips",
        description = "Gets all viewed trips.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Viewer successfully retrieved.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get trip by viewer Success",
                            name = "Successfully retrieved trips with viewer.",
                            description = "Returned trips with viewer.",
                            value= SwaggerResponseObjects.GET_TRIPS_BY_VIEWER
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trips not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getTripsByViewer(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ) : ResponseEntity<List<TripDto>> {
        val trips = tripService.getTripsByViewer(offset, limit)
        logger.info("Viewed trips found.")
        return ResponseEntity.ok(trips)
    }

    @PutMapping("/{tripId}/viewers/{viewerId}")
    @Operation(
        operationId = "addViewerToTrip",
        tags = ["Viewer"],
        summary = "Add viewer to trip",
        description = "Adds a viewer to a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Viewer successfully added.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Add viewer Success",
                            name = "Successfully added viewer to trip.",
                            description = "Returned trip with added viewer.",
                            value= SwaggerResponseObjects.ADDED_VIEWER
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to add a viewer to this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Viewer already exists in this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.CONFLICT_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun addViewerToTrip(
        @PathVariable tripId: Long,
        @PathVariable viewerId: String
    ) : ResponseEntity<TripDto> {
        val trip = tripService.addViewerToTrip(tripId, viewerId)
        logger.info("Viewer $viewerId successfully added to trip $tripId.")
        return ResponseEntity.ok(trip)
    }

    @DeleteMapping("/{tripId}/viewers/{viewerId}")
    @Operation(
        operationId = "removeViewerFromTrip",
        tags = ["Viewer"],
        summary = "Remove viewer from trip",
        description = "Removes a viewer from a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Viewer successfully removed.",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to remove a viewer from this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - Viewer missing in trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.CONFLICT_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.CONFLICT_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun removeViewerFromTrip(
        @PathVariable tripId: Long,
        @PathVariable viewerId: String,
    ) : ResponseEntity<Void> {
        tripService.removeViewerFromTrip(tripId, viewerId)
        logger.info("Viewer $viewerId successfully removed from trip $tripId.")
        return ResponseEntity.ok().build()
    }

    /*
    *   <-- Albums -->
    */

    @GetMapping("/{tripId}/albums/{albumId}")
    @Operation(
        operationId = "getAlbumFromTrip",
        tags = ["Album"],
        summary = "Get album from trip",
        description = "Gets an album by ID from trip by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album by albumId retrieved from trip.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get album by ID Success",
                            name = "Successfully retrieved album from trip.",
                            description = "Returned album from trip.",
                            value= SwaggerResponseObjects.GET_ALBUM_FROM_TRIP
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to access this data.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip/Album not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getAlbumFromTrip(
        @PathVariable tripId: Long,
        @PathVariable albumId: Long
    ) : ResponseEntity<AlbumDto> {
        val trip = tripService.getAlbumFromTrip(tripId, albumId)
        logger.info("Album $albumId retrieved from trip $trip.")
        return ResponseEntity.ok(trip)
    }

    @PostMapping("/{tripId}/albums")
    @Operation(
        operationId = "addAlbumToTrip",
        tags = ["Album"],
        summary = "Add album to trip",
        description = "Adds an album to a trip.",
        requestBody = SwaggerRequestBody(
            description = "New album information",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            name = "Example of a new album.",
                            summary = "Add album Example",
                            value = SwaggerRequestObjects.ADD_ALBUM
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album successfully added to trip.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Add album Success",
                            name = "Successfully added album to trip.",
                            description = "Returned new album.",
                            value= SwaggerResponseObjects.ADDED_ALBUM_TO_TRIP
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to add an album to this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun addAlbumToTrip(
        @PathVariable tripId: Long,
        @RequestBody dto: AlbumDto
    ) : ResponseEntity<TripDto> {
        val trip = tripService.addAlbumToTrip(tripId, dto)
        logger.info("Album ${dto.title} added to trip $tripId.")
        return ResponseEntity.ok(trip)
    }

    @DeleteMapping("/{tripId}/albums/{albumId}")
    @Operation(
        operationId = "deleteAlbumFromTrip",
        tags = ["Album"],
        summary = "Delete album from trip",
        description = "Deletes an album from a trip along with its content.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album successfully deleted from trip.",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to delete album from this trip.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun deleteAlbumFromTrip(
        @PathVariable tripId: Long,
        @PathVariable albumId: Long
    ): ResponseEntity<Void> {
        tripService.deleteAlbumFromTrip(tripId, albumId)
        logger.info("Album $albumId deleted from trip $tripId.")
        return ResponseEntity.ok().build()
    }

    /*
    *   <-- Media -->
    */

    @GetMapping("/{tripId}/media")
    @Operation(
        operationId = "getAllMediaFromTrip",
        tags = ["Media"],
        summary = "Get all media from trip",
        description = "Gets all media from a trip.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media successfully retrieved from trip.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get all media from trip Success",
                            name = "Successfully retrieved all media from trip.",
                            description = "Returned all media from trip.",
                            value= SwaggerResponseObjects.GET_ALL_MEDIA_FROM_TRIP
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - Trip not found",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.NOT_FOUND_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun getAllMediaFromTrip(
        @PathVariable tripId: Long,
    ) : ResponseEntity<List<String>> {
        val allMedia = tripService.getAllMediaFromTrip(tripId)
        logger.info("Media retrieved from trip $tripId.")
        return ResponseEntity.ok(allMedia)
    }

    @GetMapping("/search")
    @Operation(
        operationId = "searchTripsByTitle",
        tags = ["Trip"],
        summary = "Search trips by title",
        description = "Search trips by title with partial matching and pagination.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trips found.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Search trip by title Success",
                            name = "Successfully retrieved trip by title.",
                            description = "Returned trip by title.",
                            value= SwaggerResponseObjects.FOUND_TRIP_BY_TITLE
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request - Invalid query parameter.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun searchTripsByTitle(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TripDto>> {
        val trips = tripService.searchTripsByTitle(query, offset, limit)
        logger.info("Found ${trips.size} trips matching query '$query'")
        return ResponseEntity.ok(trips)
    }


    @PostMapping("/autosort")
    @Operation(
        operationId = "autosortTrip",
        tags = ["Trip"],
        summary = "Autosort trip media",
        description = "Autosort media by creation time and geolocation",
        requestBody = SwaggerRequestBody(
            description = "Trip with media to be autosorted",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Sortable trip Example",
                            name = "Example of a sortable trip.",
                            value = SwaggerRequestObjects.AUTOSORT_TRIP
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media successfully sorted.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TripDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Autosort trip Success",
                            name = "Successfully sorted trip media.",
                            description = "Returned sorted trip.",
                            value= SwaggerResponseObjects.AUTOSORTED_TRIP
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Invalid data provided.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.BAD_REQUEST_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to sort media.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.FORBIDDEN_ERROR,
                            )
                        ]
                    )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ErrorResponse::class),
                        examples = [
                            ExampleObject(
                                value= SwaggerErrorObjects.INTERNAL_SERVER_ERROR,
                            )
                        ]
                    )]
            )
        ]
    )
    fun autosortTrip(
        @RequestBody trip: TripDto,
    ): ResponseEntity<TripDto> {
        val sortedTrip = tripService.autosortTrip(trip)
        logger.info("Media from trip ${trip.title} finished autosorting.")
        return ResponseEntity.ok(sortedTrip)
    }
}
