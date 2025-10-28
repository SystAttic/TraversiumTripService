package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.service.MediaService

@RestController
@RequestMapping("/rest/v1/media")
class MediaController(
    private val mediaService: MediaService
) {

    @GetMapping
    @Operation(
        summary = "Get media by album id",
        description = "Get media by album id",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getAllMedia(): List<MediaDto> =
        mediaService.getAllMedia()

    @GetMapping("/{mediaId}")
    @Operation(
        summary = "Get media by mediaId",
        description = "Get media by mediaId",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getMediaById(@PathVariable mediaId: Long): MediaDto =
        mediaService.getMediaById(mediaId)


    // TODO - Move to AlbumController
    /*@PostMapping("/{albumId}")
    @Operation(
        summary = "Add media to album",
        description = "Add media to album by albumId",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media added to album by albumId",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun addMediaToAlbum(
        @PathVariable albumId: Long,
        @RequestBody dto: MediaDto
    ): ResponseEntity<MediaDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(mediaService.addMediaToAlbum(albumId, dto))

    @DeleteMapping("/{mediaId}")
    @Operation(
        summary = "Delete media by mediaId from album",
        description = "Delete media by mediaId",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media deleted",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun deleteMediaFromAlbum(@PathVariable mediaId: Long): ResponseEntity<Void> {
        mediaService.deleteMedia(mediaId)
        return ResponseEntity.noContent().build()
    }*/
}
