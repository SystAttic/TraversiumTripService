package traversium.tripservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.logging.log4j.kotlin.logger
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.TopicPartitionOffset
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import traversium.tripservice.dto.TripDto
import traversium.tripservice.kafka.data.ReportingStreamData
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.TripEventType
import traversium.tripservice.security.BaseSecuritySetup
import traversium.tripservice.security.MockFirebaseConfig
import traversium.tripservice.security.TestMultitenancyConfig
import traversium.tripservice.service.TripService
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["test-datastream"],
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(
    properties = [
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "kafka.reporting-topic=test-datastream",
        "kafka.bootstrap-servers=\${spring.kafka.bootstrap-servers}",
        "spring.kafka.consumer.group-id=trip-service-tests",
    ]
)
@ContextConfiguration(classes = [TripServiceKafkaTests.KafkaConsumerConfiguration::class, MockFirebaseConfig::class, TestMultitenancyConfig::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TripServiceKafkaTests() : BaseSecuritySetup() {

    @Autowired
    private lateinit var tripService: TripService

    @Autowired
    lateinit var reportingKafkaConsumer: KafkaConsumerConfiguration.ReportingKafkaConsumer

    // --- Test Constants ---
    private val COLLABORATOR_ID = "test_collab_1"
    private val VIEWER_ID = "test_viewer_1"

    @BeforeEach
    fun beforeEach() {
        reportingKafkaConsumer.clearMessages()
    }

    // -----------------------------
    // CREATE TRIP
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun createTrip_publishesEvent() {
        val dto = TripDto(
            title = "My Trip",
            description = "Test",
            ownerId = null,
            tripId = null
        )

        val savedTrip = tripService.createTrip(dto)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }

        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assert(action.eventType == TripEventType.TRIP_CREATED)
        assert(action.tripId == savedTrip.tripId)
    }

    // -----------------------------
    // CREATE ROLLBACK = NO EVENT
    // -----------------------------
    @Test
    @Transactional
    fun createTrip_rollback_emitsNoEvents() {
        val dto = TripDto(
            title = "RollbackTrip",
            description = "Test",
            ownerId = null,
            tripId = null
        )

        tripService.createTrip(dto)

        // Transaction rolls back because we are in a @Transactional test
        waitForSize(0) { reportingKafkaConsumer.getMessages().size }
    }

    // -----------------------------
    // DELETE TRIP
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteTrip_publishesEvent() {
        val dto = TripDto(
            title = "Temp trip",
            description = "test",
            ownerId = null,
            tripId = null
        )

        val saved = tripService.createTrip(dto)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        reportingKafkaConsumer.clearMessages()

        tripService.deleteTrip(saved.tripId!!)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assert(action.eventType == TripEventType.TRIP_DELETED)
        assert(action.tripId == saved.tripId)
    }

    // -----------------------------
    // UPDATE TRIP (NEW TEST)
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun updateTrip_publishesEvent() {
        val saved = createAndClearTrip()
        val newTitle = "Updated Title"
        val updatedDto = saved.copy(title = newTitle)

        tripService.updateTrip(updatedDto)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assertEquals(TripEventType.TRIP_UPDATED, action.eventType)
        assertEquals(saved.tripId, action.tripId)
        // Optionally assert the state change in the database here if needed,
        // but the Kafka test focuses on the event.
    }

    // -----------------------------
    // ADD COLLABORATOR (NEW TEST)
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun addCollaboratorToTrip_publishesEvent() {
        val saved = createAndClearTrip()

        tripService.addCollaboratorToTrip(saved.tripId!!, COLLABORATOR_ID)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assertEquals(TripEventType.COLLABORATOR_ADDED, action.eventType)
        assertEquals(saved.tripId, action.tripId)
        // Note: The event structure doesn't include the added user ID,
        // so we only confirm the type and trip ID.
    }

    // -----------------------------
    // DELETE COLLABORATOR (NEW TEST)
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteCollaboratorFromTrip_publishesEvent() {
        // 1. Setup: Create trip and add a collaborator, clearing both events
        val saved = createAndClearTrip()
        tripService.addCollaboratorToTrip(saved.tripId!!, COLLABORATOR_ID)
        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        reportingKafkaConsumer.clearMessages()

        // 2. Action: Delete the collaborator
        tripService.deleteCollaboratorFromTrip(saved.tripId!!, COLLABORATOR_ID)

        // 3. Assertion: Check for the DELETE event
        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assertEquals(TripEventType.COLLABORATOR_DELETED, action.eventType)
        assertEquals(saved.tripId, action.tripId)
    }

    // -----------------------------
    // ADD VIEWER (NEW TEST)
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun addViewerToTrip_publishesEvent() {
        val saved = createAndClearTrip()

        tripService.addViewerToTrip(saved.tripId!!, VIEWER_ID)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assertEquals(TripEventType.VIEWER_ADDED, action.eventType)
        assertEquals(saved.tripId, action.tripId)
    }

    // -----------------------------
    // DELETE VIEWER (NEW TEST)
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteViewerFromTrip_publishesEvent() {
        val saved = createAndClearTrip()
        tripService.addViewerToTrip(saved.tripId!!, VIEWER_ID)
        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        reportingKafkaConsumer.clearMessages()

        tripService.deleteViewerFromTrip(saved.tripId!!, VIEWER_ID)

        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val received = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        val action = received.action as TripEvent

        assertEquals(TripEventType.VIEWER_DELETED, action.eventType)
        assertEquals(saved.tripId, action.tripId)
    }

    // -----------------------------
    //             Extra
    // -----------------------------

    // Utility polling method
    fun waitForSize(target: Int, source: () -> Int) {
        val start = System.currentTimeMillis()
        while (source.invoke() != target && (System.currentTimeMillis() - start) <= 15000) {
            Thread.sleep(50)
        }
        if ((System.currentTimeMillis() - start) >= 15000) {
            throw IllegalStateException("Expected $target messages but got ${source.invoke()}")
        }
    }

    private fun createAndClearTrip(): TripDto {
        val dto = TripDto(title = "Temp trip", description = "test", ownerId = null, tripId = null)
        val saved = tripService.createTrip(dto)
        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        reportingKafkaConsumer.clearMessages()
        return saved
    }

    // -----------------------------
    // KAFKA CONSUMER CONFIG
    // -----------------------------
    @TestConfiguration
    class KafkaConsumerConfiguration {

        @Bean
        fun reportingKafkaConsumer() = ReportingKafkaConsumer()

        @Bean
        fun reportingKafkaListenerContainer(
            objectMapper: ObjectMapper,
            reportingKafkaConsumer: ReportingKafkaConsumer,
            @Value("\${kafka.bootstrap-servers}") bootstrapServers: String,
            @Value("\${kafka.reporting-topic}") topic: String,
            @Value("\${spring.kafka.consumer.group-id}") groupId: String) =
            KafkaMessageListenerContainer(
                reportingConsumerFactory(objectMapper, bootstrapServers, groupId),
                kafkaContainerProperties(topic, emptySet(), reportingKafkaConsumer))
                .apply {
                    commonErrorHandler = DefaultErrorHandler()
                }

        class ReportingKafkaConsumer : MessageListener<String, ReportingStreamData> {
            private val messages = LinkedBlockingQueue<Any>()

            fun getMessages(): List<Any> = messages.toList()

            fun clearMessages() = messages.clear()

            override fun onMessage(data: ConsumerRecord<String, ReportingStreamData>) {
                logger.info("--- KAFKA RECEIVED: Topic=${data.topic()}, Value=${data.value()}")
                messages.add(data.value())
            }
        }

        private fun kafkaContainerProperties(topic: String, partitions: Set<Int>, listener: MessageListener<String, *>): ContainerProperties {
            val topics = partitions.map { TopicPartitionOffset(topic, it) }.toTypedArray()
            return (if (topics.isNotEmpty()) ContainerProperties(*topics) else ContainerProperties(topic)).apply {
                isSyncCommits = true
                ackMode = ContainerProperties.AckMode.RECORD
                messageListener = listener
            }
        }

        fun reportingConsumerFactory(
            objectMapper: ObjectMapper,
            bootstrapServers: String,
            groupId: String
        ): DefaultKafkaConsumerFactory<String, ReportingStreamData> =
            DefaultKafkaConsumerFactory(
                kafkaConsumerConfig(bootstrapServers, groupId, 1048576, 1048576, ReportingStreamData::class.java),
                StringDeserializer(),
                JsonDeserializer(ReportingStreamData::class.java, objectMapper)
            )


        private fun kafkaConsumerConfig(
            bootstrapServer: String,
            groupId: String,
            fetchMaxBytes: Int,
            maxPartitionFetchBytes: Int,
            className : Class<*>): MutableMap<String, Any> =
            mutableMapOf<String, Any>().apply {
                this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
                this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
                this[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"
                this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
                this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
                this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
                this[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = org.apache.kafka.common.serialization.StringDeserializer::class.java
                this[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java
                this[ConsumerConfig.FETCH_MAX_BYTES_CONFIG] = fetchMaxBytes
                this[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = maxPartitionFetchBytes
                this[JsonDeserializer.VALUE_DEFAULT_TYPE] = className
                this[ConsumerConfig.GROUP_ID_CONFIG] = groupId
            }
    }
}
