package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.service.AlbumService

@RestController
@RequestMapping("/rest/v1/albums")
class AlbumController(
    private val albumService: AlbumService
) {

    @GetMapping
    @Operation(
        summary = "Get all albums",
        description = "Returns all albums",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "All albums",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "No albums found"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getAllAlbums(): List<AlbumDto> =
        albumService.getAllAlbums()

    @GetMapping("/{albumId}")
    @Operation(
        summary = "Get album by id",
        description = "Returns album by id",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "All albums",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getAlbumById(@PathVariable albumId: Long): AlbumDto =
        albumService.getAlbumById(albumId)

    @PostMapping("/{tripId}")
    @Operation(
        summary = "Create new album",
        description = "Creates a new album",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "New album created",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class)
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
    fun createAlbum(
        @PathVariable tripId: Long,
        @RequestBody dto: AlbumDto
    ): ResponseEntity<AlbumDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(albumService.createAlbum(tripId, dto))

    @PutMapping("/{albumId}")
    @Operation(
        summary = "Update album",
        description = "Updates an existing album",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album updated",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun updateAlbum(
        @PathVariable albumId: Long,
        @RequestBody dto: AlbumDto
    ): AlbumDto = albumService.updateAlbum(albumId, dto)

    @DeleteMapping("/{albumId}")
    @Operation(
        summary = "Delete album by id",
        description = "Deletes an existing album",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Album deleted",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = AlbumDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]

    )
    fun deleteAlbum(@PathVariable albumId: Long): ResponseEntity<Void> {
        albumService.deleteAlbum(albumId)
        return ResponseEntity.noContent().build()
    }
}
