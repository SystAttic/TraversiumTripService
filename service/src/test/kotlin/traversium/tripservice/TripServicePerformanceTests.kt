package traversium.tripservice

import org.apache.logging.log4j.kotlin.logger
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import traversium.tripservice.db.model.Visibility
import traversium.tripservice.dto.TripDto
import traversium.tripservice.security.BaseSecuritySetup
import traversium.tripservice.security.MockFirebaseConfig
import traversium.tripservice.security.TestMultitenancyConfig
import traversium.tripservice.service.ModerationServiceGrpcClient
import traversium.tripservice.service.TripService
import kotlin.system.measureTimeMillis

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@SpringBootTest(classes = [TripServiceApplication::class, TestMultitenancyConfig::class, MockFirebaseConfig::class])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TripServicePerformanceTests @Autowired constructor(
    val tripService : TripService,
    ) : BaseSecuritySetup() {

    @MockitoBean
    lateinit var moderationServiceGrpcClient: ModerationServiceGrpcClient

    @BeforeEach
    fun allowModeration() {
        `when`(moderationServiceGrpcClient.isTextAllowed(anyString())).thenReturn(true)
    }

    @Test
    fun `find all Trips for a Collaborator`() {
        val userNumber = 5000
        val tripNumber = 8000

        logger.info("Generating $userNumber users ...")
        val users = (1..userNumber).map { "user_$it" }
        logger.info("Generating $tripNumber trips ...")
        val trips = (1..tripNumber).map { id ->
            TripDto(
                tripId = null,
                title = "Trip $id",
                description = "Test trip $id",
                ownerId = users.random(),
                visibility = Visibility.PRIVATE,
                collaborators = users.shuffled().take(5).toList(),
                viewers = emptyList(),
                albums = mutableListOf()
            )
        }

        logger.info("Creating trips ...")
        trips.forEach { tripService.createTrip(it) }

        val targetUser = users.random()
        logger.info("Searching for user $targetUser ...")
        val queryTime = measureTimeMillis {
            val found = tripService.getTripsByCollaborator(targetUser)
            logger.info("Found ${found.size} trips for $targetUser")
        }

        logger.info("Query executed in $queryTime ms")
        assertTrue(queryTime < 1000, "Query too slow")
    }
}
