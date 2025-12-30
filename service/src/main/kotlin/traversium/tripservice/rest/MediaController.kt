package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.ErrorResponse
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.*
import traversium.tripservice.service.MediaService

@RestController
@RequestMapping("/rest/v1/media")
class MediaController(
    private val mediaService: MediaService
) {

    @GetMapping
    @Operation(
        operationId = "getAllMedia",
        tags = ["Media"],
        summary = "Get all media",
        description = "Gets all media.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get all media Success",
                            name = "Successfully retrieved all media.",
                            description = "Returned all media.",
                            value= SwaggerResponseObjects.GET_ALL_MEDIA
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - Unauthorized to view media.",
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
                description = "Not found - Media not found.",
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
    fun getAllMedia(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<MediaDto>> {
        val media = mediaService.getAllMedia(offset, limit)
        logger.info("All media retrieved.")
        return ResponseEntity.ok(media)
    }

    @GetMapping("/{mediaId}")
    @Operation(
        operationId = "getMediaById",
        tags = ["Media"],
        summary = "Get media",
        description = "Gets media by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get media by ID Success",
                            name = "Successfully retrieved media by ID.",
                            description = "Returned media by ID.",
                            value= SwaggerResponseObjects.GET_MEDIA_BY_ID
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
                description = "Forbidden - Unauthorized to view media.",
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
                description = "Not found - Media not found.",
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
    fun getMediaById(
        @PathVariable mediaId: Long
    ): ResponseEntity<MediaDto> {
        val media = mediaService.getMediaById(mediaId)
        logger.info("Media $mediaId retrieved.")
        return ResponseEntity.ok(media)
    }

    @GetMapping("path/{pathUrl}")
    @Operation(
        operationId = "getMediaByPathUrl",
        tags = ["Media"],
        summary = "Get media by path URL",
        description = "Gets media by path URL.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get media by path URL Success",
                            name = "Successfully retrieved media by path URL.",
                            description = "Returned media by path URL.",
                            value= SwaggerResponseObjects.GET_MEDIA_BY_PATH
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
                description = "Forbidden - Unauthorized to view media.",
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
                description = "Media not found.",
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
    fun getMediaByPathUrl(
        @PathVariable pathUrl: String
    ): ResponseEntity<MediaDto> {
        val media = mediaService.getMediaByPathUrl(pathUrl)
        logger.info("Media retrieved.")
        return ResponseEntity.ok(media)
    }

    @GetMapping("uploader/{uploaderId}")
    @Operation(
        operationId = "getMediaByUploader",
        tags = ["Media"],
        summary = "Get media by uploader",
        description = "Gets media by uploader ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media by uploader returned",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get media by uploader Success",
                            name = "Successfully retrieved media by uploader.",
                            description = "Returned media by uploader.",
                            value= SwaggerResponseObjects.GET_MEDIA_BY_UPLOADER
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
                description = "Forbidden - Unauthorized to get media.",
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
                description = "Not found - No media found.",
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
    fun getMediaByUploader(
        @PathVariable uploaderId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ) : ResponseEntity<List<MediaDto>> {
        val media = mediaService.getMediaByUploader(uploaderId, offset, limit)
        logger.info("Media found.")
        return ResponseEntity.ok(media)
    }
}
