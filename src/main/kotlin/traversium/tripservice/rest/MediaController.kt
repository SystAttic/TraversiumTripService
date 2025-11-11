package traversium.tripservice.rest

import com.google.protobuf.Api
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
import traversium.tripservice.exceptions.MediaUnauthorizedException
import traversium.tripservice.service.MediaService

@RestController
@RequestMapping("/rest/v1/media")
class MediaController(
    private val mediaService: MediaService
) {

    @GetMapping
    @Operation(
        summary = "Get all media",
        description = "Gets all media.",
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to view media.",
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
    fun getAllMedia(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<MediaDto>> {
        return try {
            val media = mediaService.getAllMedia(offset, limit)
            logger.info("All media retrieved.")
            ResponseEntity.ok(media)
        } catch (_: MediaNotFoundException) {
            logger.warn("No media found.")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{mediaId}")
    @Operation(
        summary = "Get media",
        description = "Gets media by ID.",
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
                responseCode = "403",
                description = "Forbidden - Unauthorized to view media."
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
        } catch (e: MediaUnauthorizedException) {
            logger.warn("User unauthorized to view media $mediaId.")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }catch (_: MediaNotFoundException) {
            logger.info("No media by ID $mediaId found.")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("uploader/{uploaderId}")
    @Operation(
        summary = "Get media by uploader",
        description = "Gets media by uploader ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media by uploader returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to get media."
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
    fun getMediaByUploader(
        @PathVariable uploaderId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ) : ResponseEntity<List<MediaDto>> {
        return try {
            val media = mediaService.getMediaByUploader(uploaderId, offset, limit)
            logger.info("Media found.")
            ResponseEntity.ok(media)
        } catch (_: MediaNotFoundException) {
            logger.info("No media found.")
            ResponseEntity.notFound().build()
        }
    }
}
