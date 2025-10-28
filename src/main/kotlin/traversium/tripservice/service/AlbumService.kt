package traversium.tripservice.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.db.model.Album
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.exceptions.AlbumNotFoundException
import traversium.tripservice.exceptions.TripNotFoundException
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.db.repository.TripRepository
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType

@Service
@Transactional
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val tripRepository: TripRepository,
    private val tripService: TripService,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun getAllAlbums(): List<AlbumDto> =
        albumRepository.findAll().map { it.toDto() }

    fun getAlbumById(albumId: Long): AlbumDto =
        albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }
            .toDto()

    fun createAlbum(tripId: Long, dto: AlbumDto): AlbumDto {
        val trip = tripRepository.findById(tripId).orElseThrow { TripNotFoundException(tripId) }.apply {
            albums.add(dto.toAlbum())
        }

        tripRepository.save(trip)

        // Kafka event - Album CREATE
        eventPublisher.publishEvent(
            AlbumEvent(
                eventType = AlbumEventType.ALBUM_CREATED,
                albumId = dto.albumId,
                title = dto.title,
            )
        )

        return dto
    }

    fun updateAlbum(albumId: Long, dto: AlbumDto): AlbumDto {
        val existingAlbum = albumRepository.findById(albumId)
            .orElseThrow { AlbumNotFoundException(albumId) }

        val updatedAlbum = existingAlbum.copy(
            title = dto.title,
            description = dto.description,
            media = dto.media.map { it.toMedia() }.toMutableList(),
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
        albumRepository.deleteById(albumId)
    }
}
