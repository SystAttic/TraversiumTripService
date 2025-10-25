package traversium.tripservice

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import traversium.tripservice.dto.TripDto
import traversium.tripservice.service.TripService
import kotlin.system.measureTimeMillis

@AutoConfigureTestDatabase
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TripServicePerformanceTests @Autowired constructor(
    val tripService : TripService,
    ) {

    @Test
    fun `find all Trips for a Collaborator`() {
        val userNumber = 10000
        val tripNumber = 20000

        println("Generating $userNumber users ...")
        val users = (1..userNumber).map { "user_$it" }
        println("Generating $tripNumber trips ...")
        val trips = (1..tripNumber).map { id ->
            TripDto(
                tripId = null,
                title = "Trip $id",
                description = "Test trip $id",
                ownerId = users.random(),
                collaborators = users.shuffled().take(5).toList(),
                viewers = emptyList(),
                albums = mutableListOf()
            )
        }

        println("Creating trips ...")
        trips.forEach { tripService.createTrip(it) }

        val targetUser = users.random()
        println("Searching for user $targetUser ...")
        val queryTime = measureTimeMillis {
            val found = tripService.getTripsByCollaborator(targetUser)
            println("Found ${found.size} trips for $targetUser")
        }

        println("Query executed in $queryTime ms")
        assertTrue(queryTime < 1000, "Query too slow")
    }
}
