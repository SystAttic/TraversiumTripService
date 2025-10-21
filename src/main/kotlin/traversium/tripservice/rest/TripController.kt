package traversium.tripservice.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.TripDto
import traversium.tripservice.service.TripService

@RestController
@RequestMapping("/trips")
class TripController(
    private val tripService: TripService
) {

    @GetMapping
    fun getAllTrips(): List<TripDto> = tripService.getAllTrips()

    @GetMapping("/{id}")
    fun getTripById(@PathVariable id: Long): TripDto = tripService.getTripById(id)

    @GetMapping("/owner/{owner}")
    fun getTripsByOwner(@PathVariable owner: String): List<TripDto> =
        tripService.getTripsByOwner(owner)

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
}
