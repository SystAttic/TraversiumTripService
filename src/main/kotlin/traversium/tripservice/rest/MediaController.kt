package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.service.MediaService

@RestController
@RequestMapping("/rest/v1/media")
class MediaController(
    private val mediaService: MediaService
) {

    @GetMapping
    @Operation(
        summary = "Get all media",
        description = "Get all media",
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
                responseCode = "404",
                description = "No media found."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getAllMedia(): ResponseEntity<List<MediaDto>> {
        return try {
            val media = mediaService.getAllMedia()
            logger.info("All media retrieved.")
            ResponseEntity.ok(media)
        } catch (_: MediaNotFoundException) {
            logger.info("No media found.")
            ResponseEntity.notFound().build()
        }
    }

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
                responseCode = "404",
                description = "Media not found."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getMediaById(
        @PathVariable mediaId: Long
    ): ResponseEntity<MediaDto> {
        return try {
            val media = mediaService.getMediaById(mediaId)
            logger.info("Media $mediaId retrieved.")
            ResponseEntity.ok(media)
        } catch (_: MediaNotFoundException) {
            logger.info("No media by ID $mediaId found.")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("(/owner/{ownerId})")
    @Operation(
        summary = "Get media by ownerId",
        description = "Get media by ownerId",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media by ownerId returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No media found."
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getMediaByOwner(
        @PathVariable ownerId: String
    ) : ResponseEntity<List<MediaDto>> {
        return try {
            val media = mediaService.getMediaByOwner(ownerId)
            logger.info("Media by ownerId $ownerId found.")
            ResponseEntity.ok(media)
        } catch (_: MediaNotFoundException) {
            logger.info("No media by owner $ownerId found.")
            ResponseEntity.notFound().build()
        }
    }
}
