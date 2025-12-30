package traversium.tripservice.service

import org.springframework.stereotype.Service
import traversium.tripservice.db.model.Media
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.GeoLocation
import traversium.tripservice.dto.MediaDto
import traversium.tripservice.dto.TripDto
import java.time.OffsetDateTime
import kotlin.math.pow

@Service
class AutosorterService {
    object AutoSortConfig {
        const val TIME_WINDOW_MINUTES = 90L
        const val GEO_RADIUS_METERS = 250.0

        const val MERGE_TIME_WINDOW_MINUTES = 180L
        const val MERGE_GEO_RADIUS_METERS = 500.0
    }

    data class AlbumSignature(
        val album: MutableAlbum,
        val medianTime: OffsetDateTime?,
        val centroidLocation: GeoLocation?
    )

    data class MutableAlbum(
        var albumId: Long?,
        var title: String?,
        var description: String?,
        var media: MutableList<MediaDto>
    )

    fun autoSort(trip: TripDto): TripDto {
        val allAlbums = trip.albums.map {
            MutableAlbum(it.albumId, it.title, it.description, it.media.toMutableList())
        }.toMutableList()

        val defaultAlbum = allAlbums.find { it.albumId == trip.defaultAlbum } ?: allAlbums.firstOrNull()

        val signatures = allAlbums.map { computeAlbumSignature(it) }.toMutableList()
        val processingPool = allAlbums.flatMap { it.media }.toMutableList()
        allAlbums.forEach { it.media.clear() }

        for (media in processingPool) {
            val hasTime = media.createdAt != null && media.createdAt != Media.DEFAULT_DATE
            val hasGeo = media.geoLocation != null && !media.geoLocation.hasUnknownCoordinates()

            if (!hasTime && !hasGeo) {
                defaultAlbum?.media?.add(media)
                continue
            }

            val match = findBestMatchingAlbum(media, signatures)
            if (match != null) {
                match.media.add(media)
                updateSignature(match, signatures)
            } else {
                val newAlbum = createAlbumFor(media)
                allAlbums.add(newAlbum)
                signatures.add(computeAlbumSignature(newAlbum))
            }
        }

        val mergedAlbums = mergeSimilarAlbums(allAlbums)

        val finalAlbums = mergedAlbums
            .filter { it.media.isNotEmpty() || it.albumId == trip.defaultAlbum }
            .map { album ->
                album.media.sortWith(compareBy<MediaDto> { it.createdAt }.thenBy { it.pathUrl })
                updateGeneratedTitle(album)
                AlbumDto(album.albumId, album.title, album.description, album.media)
            }

        return trip.copy(albums = finalAlbums)
    }

    private fun mergeSimilarAlbums(albums: MutableList<MutableAlbum>): List<MutableAlbum> {
        if (albums.size < 2) return albums
        val result = albums.toMutableList()
        var targetIdx = 0

        while (targetIdx < result.size) {
            val target = result[targetIdx]
            var candidateIdx = targetIdx + 1

            while (candidateIdx < result.size) {
                val candidate = result[candidateIdx]
                val sigTarget = computeAlbumSignature(target)
                val sigCandidate = computeAlbumSignature(candidate)

                if (shouldMerge(sigTarget, sigCandidate)) {
                    target.media.addAll(candidate.media)
                    resolvePreferredMetadata(target, candidate)
                    result.removeAt(candidateIdx)
                } else {
                    candidateIdx++
                }
            }
            targetIdx++
        }
        return result
    }

    private fun shouldMerge(a: AlbumSignature, b: AlbumSignature): Boolean {
        if (a.medianTime == null || b.medianTime == null || a.centroidLocation == null || b.centroidLocation == null)
            return false

        val timeDiff = java.time.Duration.between(a.medianTime, b.medianTime).abs().toMinutes()
        val spaceDiff = haversine(a.centroidLocation, b.centroidLocation)

        return timeDiff <= AutoSortConfig.MERGE_TIME_WINDOW_MINUTES &&
                spaceDiff <= AutoSortConfig.MERGE_GEO_RADIUS_METERS
    }

    fun computeAlbumSignature(album: MutableAlbum): AlbumSignature {
        val validMedia = album.media.filter { it.createdAt != Media.DEFAULT_DATE }

        val medianTime = validMedia.mapNotNull { it.createdAt }.sorted()
            .let { if (it.isNotEmpty()) it[it.size / 2] else null }

        val locations = album.media.mapNotNull { it.geoLocation }.filterNot { it.hasUnknownCoordinates() }
        val centroid = locations.takeIf { it.isNotEmpty() }?.let { locs ->
            GeoLocation(
                latitude = locs.map { it.latitude }.average(),
                longitude = locs.map { it.longitude }.average()
            )
        }

        return AlbumSignature(album, medianTime, centroid)
    }

    private fun updateSignature(album: MutableAlbum, signatures: MutableList<AlbumSignature>) {
        val index = signatures.indexOfFirst { it.album === album }
        if (index != -1) {
            signatures[index] = computeAlbumSignature(album)
        }
    }


    fun findBestMatchingAlbum(
        media: MediaDto,
        signatures: List<AlbumSignature>
    ): MutableAlbum? {
        return signatures
            .mapNotNull { sig ->
                val score = computeMatchScore(media, sig)
                if (score > 0) sig.album to score else null
            }
            .maxByOrNull { it.second }?.first
    }


    fun computeMatchScore(media: MediaDto, signature: AlbumSignature): Int {
        val mediaTime = media.createdAt?.takeIf { it != Media.DEFAULT_DATE }
        val mediaLoc = media.geoLocation?.takeIf { !it.hasUnknownCoordinates() }

        val hasTimeMatch = mediaTime != null && signature.medianTime != null
        val hasLocMatch = mediaLoc != null && signature.centroidLocation != null

        val minutesDiff = if (hasTimeMatch) {
            java.time.Duration.between(mediaTime, signature.medianTime).abs().toMinutes()
        } else Long.MAX_VALUE

        val distanceMeters = if (hasLocMatch) {
            haversine(mediaLoc!!, signature.centroidLocation!!)
        } else Double.MAX_VALUE

        if (hasTimeMatch && hasLocMatch) {
            if (minutesDiff > AutoSortConfig.TIME_WINDOW_MINUTES ||
                distanceMeters > AutoSortConfig.GEO_RADIUS_METERS) {
                return 0
            }
            return 5
        }

        if (hasTimeMatch && minutesDiff <= AutoSortConfig.TIME_WINDOW_MINUTES) return 2
        if (hasLocMatch && distanceMeters <= AutoSortConfig.GEO_RADIUS_METERS) return 3

        return 0
    }

    fun haversine(a: GeoLocation, b: GeoLocation): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = kotlin.math.sin(dLat / 2).pow(2) +
                kotlin.math.cos(Math.toRadians(a.latitude)) * kotlin.math.cos(Math.toRadians(b.latitude)) *
                kotlin.math.sin(dLon / 2).pow(2)
        return 2 * r * kotlin.math.asin(kotlin.math.sqrt(h))
    }

    fun createAlbumFor(media: MediaDto): MutableAlbum {
        val title = buildString {
            if (media.geoLocation != null && !media.geoLocation.hasUnknownCoordinates())
                append("Location (${media.geoLocation.latitude}, ${media.geoLocation.longitude}) ")
            if (media.createdAt != null && media.createdAt != Media.DEFAULT_DATE)
                append(media.createdAt.toLocalDate())
        }.ifBlank { "Unsorted" }

        return MutableAlbum(null, title, null, mutableListOf(media))
    }

    private fun updateGeneratedTitle(album: MutableAlbum) {
        if (!isTitleGenerated(album.title) || album.media.isEmpty()) return

        val signature = computeAlbumSignature(album)
        album.title = buildString {
            if (signature.centroidLocation != null) {
                val lat = String.format(java.util.Locale.US, "%.4f", signature.centroidLocation.latitude)
                val lon = String.format(java.util.Locale.US, "%.4f", signature.centroidLocation.longitude)
                append("Location ($lat, $lon) ")
            }
            if (signature.medianTime != null) {
                append(signature.medianTime.toLocalDate())
            }
        }.ifBlank { "Unsorted Cluster" }
    }

    private fun resolvePreferredMetadata(target: MutableAlbum, candidate: MutableAlbum) {
        val targetIsGenerated = isTitleGenerated(target.title)
        val candidateIsGenerated = isTitleGenerated(candidate.title)

        if (targetIsGenerated && !candidateIsGenerated) {
            target.albumId = candidate.albumId
            target.title = candidate.title
            target.description = candidate.description
        }
        else if (!targetIsGenerated && !candidateIsGenerated) {
            if (target.description != candidate.description) {
                target.description = listOfNotNull(target.description, candidate.description)
                    .joinToString(" | ")
                    .takeIf { it.isNotBlank() }
            }
        }
    }

    private fun isTitleGenerated(title: String?): Boolean {
        return title == null || title == "Unsorted" || title.startsWith("Location")
    }

}