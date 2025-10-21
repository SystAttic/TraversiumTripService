package traversium.tripservice.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.service.MediaService

@RestController
@RequestMapping("/albums/{albumId}/media")
class MediaController(
    private val mediaService: MediaService
) {

    @GetMapping
    fun getMediaForAlbum(@PathVariable albumId: Long): List<MediaDto> =
        mediaService.getMediaForAlbum(albumId)

    @GetMapping("/{mediaId}")
    fun getMediaById(@PathVariable mediaId: Long): MediaDto =
        mediaService.getMediaById(mediaId)

    @PostMapping
    fun addMedia(
        @PathVariable albumId: Long,
        @RequestBody dto: MediaDto
    ): ResponseEntity<MediaDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(mediaService.addMedia(albumId, dto))

    @DeleteMapping("/{mediaId}")
    fun deleteMedia(@PathVariable mediaId: Long): ResponseEntity<Void> {
        mediaService.deleteMedia(mediaId)
        return ResponseEntity.noContent().build()
    }
}
