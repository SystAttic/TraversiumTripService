package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.TripDto
import traversium.tripservice.service.TripService

@RestController
@RequestMapping("/trips")
class TripController(
    private val tripService: TripService
) {

    @GetMapping
    @Operation(
        summary = "Get all trips",
        description = "Get all trips",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Get all trips",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TripDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No trips not found",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
            )
        ]
    )
    fun getAllTrips(): List<TripDto> = tripService.getAllTrips()

    @GetMapping("/{id}")
    fun getTripById(@PathVariable id: Long): TripDto = tripService.getTripById(id)

    @GetMapping("/owner/{ownerId}")
    fun getTripsByOwner(@PathVariable ownerId: String): List<TripDto> =
        tripService.getTripsByOwner(ownerId)

    @PostMapping
    fun createTrip(@RequestBody tripDto: TripDto): ResponseEntity<TripDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(tripService.createTrip(tripDto))

    @PutMapping("/{id}")
    fun updateTrip(@PathVariable id: Long, @RequestBody tripDto: TripDto): TripDto =
        tripService.updateTrip(id, tripDto)

    @DeleteMapping("/{id}")
    fun deleteTrip(@PathVariable id: Long): ResponseEntity<Void> {
        tripService.deleteTrip(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{collaboratorId}")
    fun getTripsByCollaborator(@PathVariable collaboratorId: String) : ResponseEntity<Void>{
        tripService.getTripsByCollaborator(collaboratorId)
        return ResponseEntity.noContent().build()
    }
}
