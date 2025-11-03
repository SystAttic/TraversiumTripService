package traversium.tripservice.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.coyote.Response
import org.apache.logging.log4j.kotlin.logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.MediaDto
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
        description = "Returns all albums.",
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
        summary = "Get album",
        description = "Returns album by ID.",
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

    @PutMapping("/{albumId}")
    @Operation(
        summary = "Update album",
        description = "Updates an album by ID.",
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

    @GetMapping("{albumId}/media/{mediaId}")
    @Operation(
        summary = "Get media from album",
        description ="Returns media by ID from album by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media retrieved",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MediaDto::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Media not found.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getMediaFromAlbum(
        @PathVariable albumId: Long,
        @PathVariable mediaId: Long
    ) : ResponseEntity<MediaDto> {
        return try {
            val media = albumService.getMediaFromAlbum(albumId, mediaId)
            logger.info("Media $mediaId found for album $albumId.")
            ResponseEntity.ok(media)

        } catch(_: MediaNotFoundException) {
            logger.info("No media with ID $mediaId found for album $albumId.")
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("{albumId}/media")
    @Operation(
        summary = "Add media to album",
        description ="Adds media to album by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media added to album.",
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
                description = "Bad request - invalid media data provided.",
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun addMediaToAlbum(
        @PathVariable albumId: Long,
        @RequestBody mediaDto: MediaDto
    ) : ResponseEntity<AlbumDto> {
        return try {
            val album = albumService.addMediaToAlbum(albumId, mediaDto)
            logger.info("Media ${mediaDto.mediaId} added to album $albumId.")
            ResponseEntity.ok(album)
        } catch (_: AlbumNotFoundException) {
            logger.info("Album $albumId not found.")
            ResponseEntity.notFound().build()
        } catch (_: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("{albumId}/media/{mediaId}")
    @Operation(
        summary = "Delete media from album",
        description = "Deletes media by ID from album by ID.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Media successfully deleted from album.",
            ),
            ApiResponse(
                responseCode = "404",
                description = "No album found.",
            ),
            ApiResponse(
                responseCode = "409",
                description = "Bad request - no media for this id found.",
            )
        ]
    )
    fun deleteMediaFromAlbum(
        @PathVariable albumId: Long,
        @PathVariable mediaId: Long,
    ) : ResponseEntity<Void> {
        return try {
            albumService.deleteMediaFromAlbum(albumId, mediaId)
            logger.info("Media $mediaId has been deleted from album $albumId.")
            ResponseEntity.ok().build()
        } catch (_: AlbumNotFoundException){
            logger.info("Album $albumId not found.")
            ResponseEntity.notFound().build()
        } catch (_: Exception){
            ResponseEntity.badRequest().build()
        }
    }
}
