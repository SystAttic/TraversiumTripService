package traversium.tripservice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.exceptions.MediaNotFoundException
import traversium.tripservice.db.repository.MediaRepository

@Service
@Transactional
class MediaService(
    private val mediaRepository: MediaRepository,
) {

    fun getAllMedia(): List<MediaDto> =
        mediaRepository.findAll().map { it.toDto() }

    fun getMediaById(mediaId: Long): MediaDto =
        mediaRepository.findById(mediaId).orElseThrow { MediaNotFoundException(mediaId) }.toDto()

    fun getMediaByOwner(ownerId: String) : List<MediaDto> =
            mediaRepository.findByOwnerId(ownerId).map { it.toDto() }
}
