package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.ErrorResponse
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.SwaggerErrorObjects
import traversium.tripservice.exceptions.SwaggerRequestObjects
import traversium.tripservice.exceptions.SwaggerResponseObjects
import traversium.tripservice.service.AlbumService
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("/rest/v1/albums")
class AlbumController(
    private val albumService: AlbumService
) {

    @GetMapping
    @Operation(
        operationId = "getAllAlbums",
        tags = ["Album"],
        summary = "Get all albums",
        description = "Returns all albums.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "All albums",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get all albums Success",
                            name = "Successfully retrieved all albums.",
                            description = "Returned all albums.",
                            value= SwaggerResponseObjects.GET_ALL_ALBUMS
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
                description = "Not found - Albums not found.",
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
    fun getAllAlbums(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<AlbumDto>> {
        val albums = albumService.getAllAlbums(offset, limit)
        logger.info("Found ${albums.size} albums")
        return ResponseEntity.ok(albums)
    }

    @GetMapping("/{albumId}")
    @Operation(
        operationId = "getAlbumById",
        tags = ["Album"],
        summary = "Get album",
        description = "Returns an album.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "All albums",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get album by ID Success",
                            name = "Successfully retrieved album by ID.",
                            description = "Returned album by ID.",
                            value= SwaggerResponseObjects.GET_ALBUM_BY_ID
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
                description = "Forbidden - Unauthorized to view album.",
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
                description = "Not found - Album not found.",
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
    fun getAlbumById(
        @PathVariable albumId: Long
    ): ResponseEntity<AlbumDto> {
        val album = albumService.getByAlbumId(albumId)
        logger.info("Album ${album.albumId} found.")
        return ResponseEntity.ok(album)
    }

    @PutMapping("/{albumId}")
    @Operation(
        operationId = "updateAlbum",
        tags = ["Album"],
        summary = "Update album",
        description = "Updates an album.",
        requestBody = SwaggerRequestBody(
            description = "Updated album information",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Update album Example",
                            name = "Example of an updated album.",
                            value = SwaggerRequestObjects.UPDATE_ALBUM
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album updated",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Update album Success",
                            name = "Successfully updated album in trip.",
                            description = "Returned updated album.",
                            value= SwaggerResponseObjects.UPDATED_ALBUM
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
                description = "Forbidden - Unauthorized to update album.",
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
                description = "Not found - Album not found.",
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
    fun updateAlbum(
        @PathVariable albumId: Long,
        @RequestBody albumDto: AlbumDto
    ): ResponseEntity<AlbumDto> {
        val album = albumService.updateAlbum(albumId, albumDto)
        logger.info("Album ${album.albumId} updated.")
        return ResponseEntity.ok(album)
    }

    @GetMapping("{albumId}/media/{mediaId}")
    @Operation(
        operationId = "getMediaFromAlbum",
        tags = ["Media"],
        summary = "Get media from album",
        description ="Returns media from an album.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media retrieved",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Get media from Success",
                            name = "Successfully retrieved media from album.",
                            description = "Returned media from album.",
                            value= SwaggerResponseObjects.GET_MEDIA_FROM_ALBUM
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
    fun getMediaFromAlbum(
        @PathVariable albumId: Long,
        @PathVariable mediaId: Long
    ) : ResponseEntity<MediaDto> {
        val media = albumService.getMediaFromAlbum(albumId, mediaId)
        logger.info("Media $mediaId found for album $albumId.")
        return ResponseEntity.ok(media)
    }

    @PutMapping("{albumId}/media")
    @Operation(
        operationId = "addMediaToAlbum",
        tags = ["Media"],
        summary = "Add media to album",
        description ="Adds media to album by ID.",
        requestBody = SwaggerRequestBody(
            description = "Media to be added to album",
            required = true,
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class),
                    examples = [
                        ExampleObject(
                            summary = "New media Example",
                            name = "Example of a new media.",
                            value = SwaggerRequestObjects.ADD_MEDIA
                        )
                    ]
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media added to album.",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class),
                    examples = [
                        ExampleObject(
                            summary = "Add media Success",
                            name = "Successfully added media to album.",
                            description = "Returned album with new media.",
                            value= SwaggerResponseObjects.ADDED_MEDIA
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
                description = "Forbidden - Unauthorized to add media.",
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
                description = "Not found - Album not found.",
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
    fun addMediaToAlbum(
        @PathVariable albumId: Long,
        @RequestBody mediaDtos: List<MediaDto>
    ) : ResponseEntity<AlbumDto> {
        val album = albumService.addMediaToAlbum(albumId, mediaDtos)
        logger.info("Media added to album $albumId.")
        return ResponseEntity.ok(album)
    }

    @DeleteMapping("{albumId}/media/{mediaId}")
    @Operation(
        operationId = "deleteMediaFromAlbum",
        tags = ["Media"],
        summary = "Delete media from album",
        description = "Deletes media by ID from album by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media successfully deleted from album.",
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
                description = "Forbidden - Unauthorized to delete media.",
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
                description = "Not found - No album found.",
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
    fun deleteMediaFromAlbum(
        @PathVariable albumId: Long,
        @PathVariable mediaId: Long,
    ) : ResponseEntity<Void> {
            albumService.deleteMediaFromAlbum(albumId, mediaId)
            logger.info("Media $mediaId has been deleted from album $albumId.")
            return ResponseEntity.ok().build()
    }
}
