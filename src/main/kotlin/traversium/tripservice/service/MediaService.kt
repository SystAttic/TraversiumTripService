package traversium.tripservice.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import traversium.tripservice.db.model.Media
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.db.repository.MediaRepository
import traversium.tripservice.kafka.data.MediaEvent
import traversium.tripservice.kafka.data.MediaEventType

@Service
@Transactional
class MediaService(
    private val mediaRepository: MediaRepository,
    private val albumRepository: AlbumRepository,
    private val eventPublisher: ApplicationEventPublisher,
    // TODO: replace with real value of the FileStorageService
    @Value("\${filestorage.service.url:http://file-storage-service:8081}") private val fileStorageUrl: String
) {

    private val restTemplate = RestTemplate()

    fun getMediaForAlbum(albumId: Long): List<MediaDto> =
        mediaRepository.findByAlbumId(albumId).map { it.toDto() }

    fun getMediaById(mediaId: Long): MediaDto =
        mediaRepository.findById(mediaId).orElseThrow { MediaNotFoundException(mediaId) }.toDto()

    fun addMedia(albumId: Long, dto: MediaDto): MediaDto {
        val album = albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }

        // optional: validate file exists in File Storage
        try {
            val response: ResponseEntity<Map<*, *>> = restTemplate.exchange(
                "$fileStorageUrl/files/validate?url=${dto.pathUrl}",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Map::class.java
            )
            if (response.statusCode.isError) {
                println("FileStorageService validation failed for ${dto.pathUrl}")
            }
        } catch (e: Exception) {
            println("Could not validate file in FileStorageService: ${e.message}")
        }

        val media = Media(
            album = album,
            pathUrl = dto.pathUrl,
            ownerId = dto.ownerId,
            fileType = dto.fileType,
            fileFormat = dto.fileFormat,
            fileSize = dto.fileSize,
            geoLocation = dto.geoLocation,
            timeCreated = dto.timeCreated
        )
        // Kafka event - Media ADD
        eventPublisher.publishEvent(
            MediaEvent(
                eventType = MediaEventType.MEDIA_ADDED,
                mediaId = media.mediaId,
                albumId = albumId,
                pathUrl = media.pathUrl,
            )
        )

        return mediaRepository.save(media).toDto()
    }

    fun deleteMedia(mediaId: Long) {
        val existingMedia = mediaRepository.findById(mediaId)
            .orElseThrow { MediaNotFoundException(mediaId) }

        // Optional: notify FileStorageService
        try {
            restTemplate.delete("$fileStorageUrl/files/delete-by-url?url=${existingMedia.pathUrl}")
        } catch (e: Exception) {
            println("Could not notify FileStorageService: ${e.message}")
        }

        // Kafka event - Media DELETE
        eventPublisher.publishEvent(
            MediaEvent(
                eventType = MediaEventType.MEDIA_DELETED,
                mediaId = existingMedia.mediaId,
                albumId = existingMedia.album!!.albumId,
                pathUrl = existingMedia.pathUrl,
            )
        )
        mediaRepository.delete(existingMedia)
    }
}
