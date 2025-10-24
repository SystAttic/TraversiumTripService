package traversium.tripservice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import traversium.tripservice.db.model.Album
import traversium.tripservice.db.model.Trip
import traversium.tripservice.db.repository.AlbumRepository
import traversium.tripservice.db.repository.TripRepository
import java.util.Collections.emptySet


@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TripServiceTests @Autowired constructor(
    private val tripRepository: TripRepository,
    private val albumRepository: AlbumRepository
) {


    @Test
    fun test() {
        val trip1 = Trip(tripId = null, title = "Trip1", description = null, ownerId = "P1", coverPhotoUrl = "", collaborators = setOf("P2","P3"), viewers = emptySet())
        val trip2 = Trip(tripId = null, title = "Trip2", description = null, ownerId = "P2", coverPhotoUrl = "", collaborators = setOf("P3","P2"), viewers = emptySet())
        val trip3 = Trip(tripId = null, title = "Trip3", description = null, ownerId = "P3", coverPhotoUrl = "", collaborators = setOf("P2","P1"), viewers = emptySet())

        val savedTrip1 = tripRepository.save(trip1)
        val savedTrip2 = tripRepository.save(trip2)
        val savedTrip3 = tripRepository.save(trip3)


//        val album1 = Album(albumId = null, trip = savedTrip1, title = "Album1 Tripa 1", description = "", media = emptySet())
        val album1 = Album(albumId = null, title = "Album1 Tripa 1", description = "", media = emptySet())
        val album2 = Album(albumId = null, title = "Album2 Tripa 1", description = "", media = emptySet())
        val album3 = Album(albumId = null, title = "Album3 Tripa 1", description = "", media = emptySet())
        val albumsTrip1 = mutableSetOf(
            album1,
            album2,
            album3,
        )
//        val album4 = Album(albumId = null, trip = savedTrip2, title = "Album1 Tripa 2", description = "", media = emptySet())
//        val album5 = Album(albumId = null, trip = savedTrip2, title = "Album2 Tripa 2", description = "", media = emptySet())
//        val album6 = Album(albumId = null, trip = savedTrip2, title = "Album3 Tripa 2", description = "", media = emptySet())
//        val albumsTrip2 = mutableSetOf(
//            album4,
//            album5,
//            album6,
//        )
//        val album7 = Album(albumId = null, trip = savedTrip3, title = "Album1 Tripa 3", description = "", media = emptySet())
//        val album8 = Album(albumId = null, trip = savedTrip3, title = "Album2 Tripa 3", description = "", media = emptySet())
//        val album9 = Album(albumId = null, trip = savedTrip3, title = "Album3 Tripa 3", description = "", media = emptySet())
//        val albumsTrip3 = mutableSetOf(
//            album7,
//            album8,
//            album9,
//        )

//        albumRepository.save(album1)
//        albumRepository.save(album2)
//        albumRepository.save(album3)
//        albumRepository.save(album4)
//        albumRepository.save(album5)
//        albumRepository.save(album6)
//        albumRepository.save(album7)
//        albumRepository.save(album8)
//        albumRepository.save(album9)

        savedTrip1.apply { albums = albumsTrip1 }
//        savedTrip2.apply { albums = albumsTrip2 }
//        savedTrip3.apply { albums = albumsTrip3 }

        tripRepository.save(savedTrip1)
//        tripRepository.save(savedTrip2)
//        tripRepository.save(savedTrip3)


        // Test - get trips by Owner ID
        val found = tripRepository.findByOwnerId("P1")
        val foundAlbum = albumRepository.findById(found[0].albums.first().albumId!!)
//        assertEquals("Album1 Tripa 1", foundAlbum.get().title)
        assertEquals(1, found.count())
        assertEquals(3, found[0].albums.count())

        //TODO - test hitrosti iskanja po Tripih (by Collaborators)
        // naredi zanko za izdelavo userjev (npr. naredi temp_user in ga "ustvari" in dodaj v users[])
        // naredi podobno za trips
        // nato testiraj queries za ta vecji nabor
        // pred searchom nastavi offset datetime na "Now" in po searchu, da vidimo čas

    }
}