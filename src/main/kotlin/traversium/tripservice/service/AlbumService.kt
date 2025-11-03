package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.AlbumWithoutMediaException
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.MediaEvent
import traversium.tripservice.kafka.data.MediaEventType

@Service
@Transactional
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun getAllAlbums(): List<AlbumDto> =
        albumRepository.findAll().map { it.toDto() }

    fun getByAlbumId(albumId: Long): AlbumDto =
        albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }
            .toDto()

    @Transactional
    fun createAlbum(dto: AlbumDto): AlbumDto {
        if (dto.title == null || dto.albumId != null) {
            throw IllegalArgumentException("Title cannot be null, new album cannot have albumId")
        }

        val album = albumRepository.save(dto.toAlbum())

        // Kafka event - Trip CREATE
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_CREATED,
                albumId = album.albumId,
                title = album.title,
            )
        )
        return album.toDto()
    }

    @Transactional
    fun updateAlbum(albumId: Long, dto: AlbumDto): AlbumDto {
        val existingAlbum = albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }

        val updatedAlbum = existingAlbum.copy(
            title = dto.title ?: existingAlbum.title,
            description = dto.description ?: existingAlbum.description,
        )
        // Kafka event - Album UPDATE
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_UPDATED,
                albumId = updatedAlbum.albumId,
                title = updatedAlbum.title,
            )
        )
        return albumRepository.save(updatedAlbum).toDto()
    }

    @Transactional
    fun deleteAlbum(albumId: Long) {
        val album = albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }
        // Kafka event - Album DELETE
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_DELETED,
                albumId = album.albumId,
                title = album.title,
            )
        )
        albumRepository.delete(album)
    }


    fun getMediaFromAlbum(albumId: Long, mediaId: Long): MediaDto {
        val album = albumRepository.findById(albumId).orElseThrow { AlbumNotFoundException(albumId) }
        if(album.media.isEmpty())
            throw AlbumWithoutMediaException(albumId)
        else
            return album.media.first{ it.mediaId == mediaId }.toDto()
    }

    @Transactional
    fun addMediaToAlbum(albumId: Long, dto: MediaDto) : AlbumDto {
        val album = albumRepository.findById(albumId).orElseThrow{ AlbumNotFoundException(albumId) }
        album.media.add(dto.toMedia())

        // Kafka event - Media CREATE
        eventPublisher.publishEvent(
            MediaEvent(
                eventType = MediaEventType.MEDIA_ADDED,
                mediaId = dto.mediaId,
                pathUrl = dto.pathUrl,

            )
        )
        return albumRepository.save(album).toDto()
    }

    @Transactional
    fun deleteMediaFromAlbum(albumId: Long, mediaId: Long) {
        val album = albumRepository.findById(albumId).orElseThrow { AlbumNotFoundException(albumId) }

        if(album.media.any { it.mediaId == mediaId }) {
            val media = album.media.find { it.mediaId == mediaId }

            // Kafka event - Media DELETE
            eventPublisher.publishEvent(
                MediaEvent(
                    eventType = MediaEventType.MEDIA_DELETED,
                    mediaId = media!!.mediaId,
                    pathUrl = media.pathUrl,

                )
            )
            album.media.remove(media)
            albumRepository.save(album)
        } else
            throw AlbumWithoutMediaException(albumId)
    }
}
