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
import traversium.tripservice.db.repository.TripRepository


@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TripServiceTests @Autowired constructor(
    private val tripRepository: TripRepository
) {
    val albumsTrip1 = listOf(
        Album(albumId=null, trip=null, title="Album1 Tripa 1", description="", media=emptyList()),
        Album(albumId=null, trip=null, title="Album2 Tripa 1", description="", media=emptyList()),
        Album(albumId=null, trip=null, title="Album3 Tripa 1", description="", media=emptyList()),
    )
    val albumsTrip2 = listOf(
        Album(albumId=null, trip=null, title="Album1 Tripa 2", description="", media=emptyList()),
        Album(albumId=null, trip=null, title="Album2 Tripa 2", description="", media=emptyList()),
        Album(albumId=null, trip=null, title="Album3 Tripa 2", description="", media=emptyList()),
    )
    val albumsTrip3 = listOf(
        Album(albumId=null, trip=null, title="Album1 Tripa 3", description="", media=emptyList()),
        Album(albumId=null, trip=null, title="Album2 Tripa 3", description="", media=emptyList()),
        Album(albumId=null, trip=null, title="Album3 Tripa 3", description="", media=emptyList()),
    )

    @Test
    fun test() {
        val trip1 = Trip(tripId = null, title = "Trip1", description = null, owner = "P1", coverPhotoUrl = "", collaborators = setOf("P2","P3"), viewers = emptySet(), albums=(albumsTrip1))
        val trip2 = Trip(tripId = null, title = "Trip2", description = null, owner = "P2", coverPhotoUrl = "", collaborators = setOf("P3","P2"), viewers = emptySet(), albums=(albumsTrip2))
        val trip3 = Trip(tripId = null, title = "Trip3", description = null, owner = "P3", coverPhotoUrl = "", collaborators = setOf("P2","P1"), viewers = emptySet(), albums=(albumsTrip3))

        tripRepository.save(trip1)
        tripRepository.save(trip2)
        tripRepository.save(trip3)

        // Test - get trips by Owner ID
        val found = tripRepository.findByOwner("P1")
        assertEquals(found.count(), 1)

        //TODO - test hitrosti iskanja po Tripih (by Collaborators)
        // naredi zanko za izdelavo userjev (npr. naredi temp_user in ga "ustvari" in dodaj v users[])
        // naredi podobno za trips
        // nato testiraj queries za ta vecji nabor
        // pred searchom nastavi offset datetime na "Now" in po searchu, da vidimo čas

    }
}