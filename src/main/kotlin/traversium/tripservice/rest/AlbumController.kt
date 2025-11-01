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
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.exceptions.*
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
    fun getAllAlbums(): ResponseEntity<List<AlbumDto>> {
        return try{
            val albums = albumService.getAllAlbums()
            logger.info("Found ${albums.size} albums")
            ResponseEntity.ok(albums)
        } catch (_: AlbumNotFoundException) {
            logger.info("")
            ResponseEntity.notFound().build()
        }

    }
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
    fun getAlbumById(
        @PathVariable albumId: Long
    ): ResponseEntity<AlbumDto> {
        return try {
            val album = albumService.getByAlbumId(albumId)
            logger.info("Album ${album.albumId} found.")
            ResponseEntity.ok(album)
        } catch (_: AlbumNotFoundException) {
            logger.info("Album with ID $albumId not found.")
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{albumId}")
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
                description = "Bad request - invalid album data provided.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - album already exists.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]

    )
    fun createAlbum(
        @RequestBody albumDto: AlbumDto
    ): ResponseEntity<AlbumDto> {
        return try {
            val album = albumService.createAlbum(albumDto)
            logger.info("Album ${album.albumId} created.")
            ResponseEntity.ok(album)
        } catch (_: AlbumAlreadyExistsException) {
            logger.info("Album ${albumDto.albumId} already exists.")
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (_: Exception){
            ResponseEntity.badRequest().build()
        }
    }

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
                responseCode = "404",
                description = "Album not found.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - invalid album data provided.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error.",
            )
        ]
    )
    fun updateAlbum(
        @PathVariable albumId: Long,
        @RequestBody albumDto: AlbumDto
    ): ResponseEntity<AlbumDto> {
        return try {
            val album = albumService.updateAlbum(albumId, albumDto)
            logger.info("Album ${album.albumId} updated.")
            ResponseEntity.ok(album)
        } catch (_: AlbumNotFoundException) {
            logger.info("Album ${albumDto.albumId} not found.")
            ResponseEntity.notFound().build()
        } catch (_: Exception){
            ResponseEntity.badRequest().build()
        }
    }

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
                responseCode = "404",
                description = "Album not found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]

    )
    fun deleteAlbum(
        @PathVariable albumId: Long
    ): ResponseEntity<Void> {
        return try {
            albumService.deleteAlbum(albumId)
            logger.info("Trip $albumId deleted.")
            ResponseEntity.ok().build()
        } catch (_: AlbumNotFoundException){
            logger.info("Trip $albumId not found.")
            ResponseEntity.notFound().build()
        }
    }
}
