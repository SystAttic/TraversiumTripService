package traversium.tripservice.kafka.data

data class UserBlockedEvent(
        val blockerId: String,
        val blockedId: String,
        val timestamp: Long = System.currentTimeMillis()
)