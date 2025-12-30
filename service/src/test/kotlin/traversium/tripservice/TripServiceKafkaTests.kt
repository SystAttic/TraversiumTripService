package traversium.tripservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.given
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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import traversium.audit.kafka.AuditStreamData
import traversium.audit.kafka.TripActivityAction
import traversium.notification.kafka.ActionType
import traversium.notification.kafka.NotificationStreamData
import traversium.tripservice.dto.AlbumDto
import traversium.tripservice.dto.TripDto
import traversium.tripservice.kafka.data.AlbumEvent
import traversium.tripservice.kafka.data.AlbumEventType
import traversium.tripservice.kafka.data.ReportingStreamData
import traversium.tripservice.kafka.data.TripEvent
import traversium.tripservice.kafka.data.TripEventType
import traversium.tripservice.security.BaseSecuritySetup
import traversium.tripservice.security.MockFirebaseConfig
import traversium.tripservice.security.TestMultitenancyConfig
import traversium.tripservice.service.AlbumService
import traversium.tripservice.service.ModerationServiceGrpcClient
import traversium.tripservice.service.TripService
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [TripServiceApplication::class]
)
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    topics = ["test-datastream", "test-notifications", "test-audits"],
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(
    properties = [
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.reporting-topic=test-datastream",
        "spring.kafka.consumer.group-id=trip-service-tests",
        "spring.kafka.notification-topic=test-notifications",
        "spring.kafka.audit-topic=test-audits",
        "spring.cloud.config.enabled=false"
    ]
)
@ContextConfiguration(classes = [TripServiceKafkaTests.KafkaConsumerConfiguration::class, MockFirebaseConfig::class, TestMultitenancyConfig::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TripServiceKafkaTests() : BaseSecuritySetup() {

    @Autowired
    private lateinit var tripService: TripService

    @Autowired
    private lateinit var albumService: AlbumService

    @Autowired
    lateinit var reportingKafkaConsumer: KafkaConsumerConfiguration.ReportingKafkaConsumer

    @Autowired
    lateinit var notificationKafkaConsumer: KafkaConsumerConfiguration.NotificationKafkaConsumer

    @Autowired
    lateinit var auditingKafkaConsumer: KafkaConsumerConfiguration.AuditingKafkaConsumer

    @MockitoBean
    private lateinit var moderationServiceGrpcClient: ModerationServiceGrpcClient

    // --- Test Constants ---
    private val COLLABORATOR_ID = "test_collab_1"
    private val VIEWER_ID = "test_viewer_1"

    @BeforeEach
    fun beforeEach() {
        given(moderationServiceGrpcClient.isTextAllowed(any()))
            .willReturn(true)


        reportingKafkaConsumer.clearMessages()
        notificationKafkaConsumer.clearMessages()
        auditingKafkaConsumer.clearMessages()
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

        val saved = tripService.createTrip(dto)

        testKafkaMessages(saved, TripEventType.TRIP_CREATED, ActionType.CREATE, TripActivityAction.TRIP_CREATED)
    }

    // -----------------------------
    // CREATE TRIP ROLLBACK
    // -----------------------------
    @Test
    @Transactional
    fun createTrip_rollback() {
        val dto = TripDto(
            title = "RollbackTrip",
            description = "Test",
            ownerId = null,
            tripId = null
        )

        tripService.createTrip(dto)

        waitForSize(0) { reportingKafkaConsumer.getMessages().size }
        waitForSize(0) { notificationKafkaConsumer.getMessages().size }
        waitForSize(0) { auditingKafkaConsumer.getMessages().size }
    }

    // -----------------------------
    // DELETE TRIP
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteTrip_publishesEvent() {
        val saved = createAndClearTrip()

        tripService.deleteTrip(saved.tripId!!)

        testKafkaMessages(saved, TripEventType.TRIP_DELETED, ActionType.DELETE, TripActivityAction.TRIP_DELETED)
    }

    // -----------------------------
    // UPDATE TRIP
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun updateTrip_publishesEvent() {
        val saved = createAndClearTrip()
        val newTitle = "Updated Title"
        val updatedDto = saved.copy(title = newTitle)

        tripService.updateTrip(updatedDto)

        testKafkaMessages(saved, TripEventType.TRIP_UPDATED, ActionType.CHANGE_TITLE, TripActivityAction.TRIP_NAME_CHANGED)
    }

    // -----------------------------
    // ADD COLLABORATOR
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun addCollaboratorToTrip_publishesEvent() {
        val saved = createAndClearTrip()

        tripService.addCollaboratorToTrip(saved.tripId!!, COLLABORATOR_ID)

        //TODO - update when EntityType for Collaborator changes to COLLABORATOR
        testKafkaMessages(saved, TripEventType.COLLABORATOR_ADDED, ActionType.ADD_COLLABORATOR, TripActivityAction.TRIP_COLLABORATOR_INVITED)
    }

    // -----------------------------
    // DELETE COLLABORATOR
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteCollaboratorFromTrip_publishesEvent() {
        val saved = createAndClearTrip()
        tripService.addCollaboratorToTrip(saved.tripId!!, COLLABORATOR_ID)
        recieveAndClearKafkaMessages()

        tripService.removeCollaboratorFromTrip(saved.tripId!!, COLLABORATOR_ID)

        //TODO - update when EntityType for Collaborator changes to COLLABORATOR
        testKafkaMessages(saved, TripEventType.COLLABORATOR_DELETED, ActionType.REMOVE_COLLABORATOR, TripActivityAction.TRIP_COLLABORATOR_REMOVED)
    }

    // -----------------------------
    // ADD VIEWER
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun addViewerToTrip_publishesEvent() {
        val saved = createAndClearTrip()

        tripService.addViewerToTrip(saved.tripId!!, VIEWER_ID)

        //TODO - update when EntityType for Viewer changes to VIEWER
        testKafkaMessages(saved, TripEventType.VIEWER_ADDED, ActionType.ADD_VIEWER, TripActivityAction.TRIP_VIEWER_INVITED)
    }

    // -----------------------------
    // DELETE VIEWER
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteViewerFromTrip_publishesEvent() {
        val saved = createAndClearTrip()
        tripService.addViewerToTrip(saved.tripId!!, VIEWER_ID)
        recieveAndClearKafkaMessages()

        tripService.removeViewerFromTrip(saved.tripId!!, VIEWER_ID)

        //TODO - update when EntityType for Viewer changes to VIEWER
        testKafkaMessages(saved, TripEventType.VIEWER_DELETED, ActionType.REMOVE_VIEWER, TripActivityAction.TRIP_VIEWER_REMOVED)
    }

    // -----------------------------
    //            Albums
    // -----------------------------

    // -----------------------------
    // CREATE ALBUM
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun createAlbum_publishesEvent() {
        val tripDto = TripDto(
            title = "My Trip",
            description = "Test",
            ownerId = null,
            tripId = null
        )
        val albumDto = AlbumDto(
            albumId = null,
            title = "My Album",
            description = "Test",
        )

        val savedTrip = tripService.createTrip(tripDto)
        recieveAndClearKafkaMessages()

        val savedAlbum = tripService.addAlbumToTrip(savedTrip.tripId!!, albumDto)
        val saved = tripService.getAlbumFromTrip(savedTrip.tripId!!, savedAlbum.albums.last().albumId!!)

        testKafkaMessages(saved, AlbumEventType.ALBUM_CREATED, ActionType.CREATE, TripActivityAction.ALBUM_CREATED)
    }

    // -----------------------------
    // CREATE ALBUM ROLLBACK
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun createAlbum_rollback() {
        val tripDto = TripDto(
            title = "My Trip",
            description = "Test",
            ownerId = null,
            tripId = null
        )
        val albumDto = AlbumDto(
            albumId = null,
            title = "My Rollbacked Album",
            description = "Test",
        )

        val savedTrip = tripService.createTrip(tripDto)
        recieveAndClearKafkaMessages()

        tripService.addAlbumToTrip(savedTrip.tripId!!, albumDto)

        waitForSize(0) { reportingKafkaConsumer.getMessages().size }
        waitForSize(0) { notificationKafkaConsumer.getMessages().size }
        waitForSize(0) { auditingKafkaConsumer.getMessages().size }
    }

    // -----------------------------
    // DELETE ALBUM
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun deleteAlbum_publishesEvent() {
        val tripDto = TripDto(
            title = "My Trip",
            description = "Test",
            ownerId = null,
            tripId = null
        )
        val albumDto = AlbumDto(
            albumId = null,
            title = "My Album",
            description = "Test",
        )

        val savedTrip = tripService.createTrip(tripDto)
        recieveAndClearKafkaMessages()
        tripService.addAlbumToTrip(savedTrip.tripId!!, albumDto)
        recieveAndClearKafkaMessages()

        val deletedAlbum = tripService.getAlbumFromTrip(savedTrip.tripId!!, 2L)
        tripService.deleteAlbumFromTrip(savedTrip.tripId!!, 2L)

        testKafkaMessages(deletedAlbum, AlbumEventType.ALBUM_DELETED, ActionType.DELETE, TripActivityAction.ALBUM_DELETED)
    }

    // -----------------------------
    // UPDATE ALBUM
    // -----------------------------
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun updateAlbum_publishesEvent() {
        val tripDto = TripDto(
            title = "My Trip",
            description = "Test",
            ownerId = null,
            tripId = null
        )
        val albumDto = AlbumDto(
            albumId = null,
            title = "My Album",
            description = "Test",
        )
        val newAlbumDto = AlbumDto(
            albumId = null,
            title = "My New Album",
        )

        val savedTrip = tripService.createTrip(tripDto)
        recieveAndClearKafkaMessages()
        val savedAlbum = tripService.addAlbumToTrip(savedTrip.tripId!!, albumDto)
        recieveAndClearKafkaMessages()

        val updatedAlbum = albumService.updateAlbum(savedAlbum.albums.first().albumId!!, newAlbumDto)

        val saved = tripService.getAlbumFromTrip(savedTrip.tripId!!, updatedAlbum.albumId!!)

        testKafkaMessages(saved, AlbumEventType.ALBUM_UPDATED, ActionType.CHANGE_TITLE, TripActivityAction.ALBUM_TITLE_CHANGED)
    }


    // -----------------------------
    //             Extra
    // -----------------------------

    // Utility polling method
    fun waitForSize(target: Int, source: () -> Int) {
        val start = System.currentTimeMillis()
        while (source.invoke() != target && (System.currentTimeMillis() - start) <= 3000) {
            Thread.sleep(50)
        }
        if ((System.currentTimeMillis() - start) >= 15000) {
            throw IllegalStateException("Expected $target messages but got ${source.invoke()}")
        }
    }

    fun waitUntilAtLeast(target: Int, source: () -> Int) {
        val start = System.currentTimeMillis()
        while (source.invoke() < target && (System.currentTimeMillis() - start) <= 3000) {
            Thread.sleep(50)
        }
        if (source.invoke() < target) {
            throw IllegalStateException("Expected at least $target messages but got ${source.invoke()}")
        }
    }


    private fun createAndClearTrip(): TripDto {
        val dto = TripDto(title = "Temp trip", description = "test", ownerId = null, tripId = null)
        val saved = tripService.createTrip(dto)

        recieveAndClearKafkaMessages()

        return saved
    }

    private fun recieveAndClearKafkaMessages() {
        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        waitForSize(1) { notificationKafkaConsumer.getMessages().size }
        waitForSize(1) { auditingKafkaConsumer.getMessages().size }
        reportingKafkaConsumer.clearMessages()
        notificationKafkaConsumer.clearMessages()
        auditingKafkaConsumer.clearMessages()
    }

    private fun <T> testKafkaMessages(
        entity: T,
        reportingActionType: Any,
        notificationActionType: Any,
        auditingActionType: TripActivityAction
    ) {
        waitForSize(1) { reportingKafkaConsumer.getMessages().size }
        val reporting = reportingKafkaConsumer.getMessages()[0] as ReportingStreamData
        when (entity) {
            is TripDto -> {
                val action = reporting.action as TripEvent
                assertEquals(reportingActionType, action.eventType)
                assertEquals(entity.tripId, action.tripId)
            }
            is AlbumDto -> {
                val action = reporting.action as AlbumEvent
                assertEquals(reportingActionType, action.eventType)
                assertEquals(entity.albumId, action.albumId)
                // TODO: add test for Media ADD/DELETE (here because addMediaToAlbum and deleteMediaFromAlbum return AlbumDto)
            }
            else -> error("Unsupported entity type: ${entity!!::class}")
        }

        waitUntilAtLeast(1) { notificationKafkaConsumer.getMessages().size }
        val notification = notificationKafkaConsumer.getMessages().first() as NotificationStreamData
        assertEquals(notificationActionType, notification.action)

        waitUntilAtLeast(1) { auditingKafkaConsumer.getMessages().size }
        val audit = auditingKafkaConsumer.getMessages().first() as AuditStreamData
        assertEquals(auditingActionType.name, audit.action)

        when (entity){
            is TripDto -> assertEquals(entity.tripId, audit.entityId)
            is AlbumDto -> assertEquals(entity.albumId, audit.entityId)
            // TODO: add test for Media ADD/DELETE (handle under AlbumDto because addMediaToAlbum and deleteMediaFromAlbum return AlbumDto)
            else -> error("Unsupported entity type: ${entity!!::class}")
        }
    }

    // -----------------------------
    // KAFKA CONSUMER CONFIG
    // -----------------------------
    @TestConfiguration
    class KafkaConsumerConfiguration {

        @Bean
        fun reportingKafkaConsumer() = ReportingKafkaConsumer()

        @Bean
        fun notificationKafkaConsumer() = NotificationKafkaConsumer()

        @Bean
        fun auditingKafkaConsumer() = AuditingKafkaConsumer()

        @Bean
        fun reportingKafkaListenerContainer(
            objectMapper: ObjectMapper,
            reportingKafkaConsumer: ReportingKafkaConsumer,
            @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String,
            @Value("\${spring.kafka.reporting-topic}") topic: String,
            @Value("\${spring.kafka.consumer.group-id}") groupId: String) =
            KafkaMessageListenerContainer(
                reportingConsumerFactory(objectMapper, bootstrapServers, groupId),
                kafkaContainerProperties(topic, emptySet(), reportingKafkaConsumer))
                .apply {
                    commonErrorHandler = DefaultErrorHandler()
                }

        @Bean
        fun notificationKafkaListenerContainer(
            objectMapper: ObjectMapper,
            notificationKafkaConsumer: NotificationKafkaConsumer,
            @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String,
            @Value("\${spring.kafka.notification-topic}") topic: String,
            @Value("\${spring.kafka.consumer.group-id}") groupId: String) =
            KafkaMessageListenerContainer(
                notificationConsumerFactory(objectMapper, bootstrapServers, groupId),
                kafkaContainerProperties(topic, emptySet(), notificationKafkaConsumer))
                .apply {
                    commonErrorHandler = DefaultErrorHandler()
                }

        @Bean
        fun auditingKafkaListenerContainer(
            objectMapper: ObjectMapper,
            auditingKafkaConsumer: AuditingKafkaConsumer,
            @Value("\${spring.kafka.bootstrap-servers}") bootstrapServers: String,
            @Value("\${spring.kafka.audit-topic}") topic: String,
            @Value("\${spring.kafka.consumer.group-id}") groupId: String) =
            KafkaMessageListenerContainer(
                auditingConsumerFactory(objectMapper, bootstrapServers, groupId),
                kafkaContainerProperties(topic, emptySet(), auditingKafkaConsumer))
                .apply {
                    commonErrorHandler = DefaultErrorHandler()
                }

        class ReportingKafkaConsumer : MessageListener<String, ReportingStreamData> {
            private val messages = LinkedBlockingQueue<Any>()

            fun getMessages(): List<Any> = messages.toList()

            fun clearMessages() = messages.clear()

            override fun onMessage(data: ConsumerRecord<String, ReportingStreamData>) {
                messages.add(data.value())
            }
        }

        class NotificationKafkaConsumer : MessageListener<String, NotificationStreamData> {
            private val messages = LinkedBlockingQueue<Any>()

            fun getMessages(): List<Any> = messages.toList()

            fun clearMessages() = messages.clear()

            override fun onMessage(data: ConsumerRecord<String, NotificationStreamData>) {
                messages.add(data.value())
            }
        }

        class AuditingKafkaConsumer : MessageListener<String, AuditStreamData> {
            private val messages = LinkedBlockingQueue<Any>()

            fun getMessages(): List<Any> = messages.toList()

            fun clearMessages() = messages.clear()

            override fun onMessage(data: ConsumerRecord<String, AuditStreamData>) {
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

        fun notificationConsumerFactory(
            objectMapper: ObjectMapper,
            bootstrapServers: String,
            groupId: String
        ): DefaultKafkaConsumerFactory<String, NotificationStreamData> =
            DefaultKafkaConsumerFactory(
                kafkaConsumerConfig(bootstrapServers, groupId, 1048576, 1048576, NotificationStreamData::class.java),
                StringDeserializer(),
                JsonDeserializer(NotificationStreamData::class.java, objectMapper)
            )

        fun auditingConsumerFactory(
            objectMapper: ObjectMapper,
            bootstrapServers: String,
            groupId: String
        ): DefaultKafkaConsumerFactory<String, AuditStreamData> =
            DefaultKafkaConsumerFactory(
                kafkaConsumerConfig(bootstrapServers, groupId, 1048576, 1048576, AuditStreamData::class.java),
                StringDeserializer(),
                JsonDeserializer(AuditStreamData::class.java, objectMapper)
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
                this[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = StringDeserializer::class.java
                this[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = JsonDeserializer::class.java
                this[ConsumerConfig.FETCH_MAX_BYTES_CONFIG] = fetchMaxBytes
                this[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = maxPartitionFetchBytes
                this[JsonDeserializer.VALUE_DEFAULT_TYPE] = className
                this[ConsumerConfig.GROUP_ID_CONFIG] = groupId
            }
    }
}
