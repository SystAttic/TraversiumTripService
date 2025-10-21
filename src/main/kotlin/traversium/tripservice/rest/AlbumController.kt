package traversium.tripservice.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.service.AlbumService

@RestController
@RequestMapping("/trips/{tripId}/albums")
class AlbumController(
    private val albumService: AlbumService
) {

    @GetMapping
    fun getAlbumsForTrip(@PathVariable tripId: Long): List<AlbumDto> =
        albumService.getAlbumsForTrip(tripId)

    @GetMapping("/{albumId}")
    fun getAlbumById(@PathVariable albumId: Long): AlbumDto =
        albumService.getAlbumById(albumId)

    @PostMapping
    fun createAlbum(
        @PathVariable tripId: Long,
        @RequestBody dto: AlbumDto
    ): ResponseEntity<AlbumDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(albumService.createAlbum(tripId, dto))

    @PutMapping("/{albumId}")
    fun updateAlbum(
        @PathVariable albumId: Long,
        @RequestBody dto: AlbumDto
    ): AlbumDto = albumService.updateAlbum(albumId, dto)

    @DeleteMapping("/{albumId}")
    fun deleteAlbum(@PathVariable albumId: Long): ResponseEntity<Void> {
        albumService.deleteAlbum(albumId)
        return ResponseEntity.noContent().build()
    }
}
