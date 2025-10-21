package traversium.tripservice.kafka

enum class PartitioningStrategy {
    ROUND_ROBIN,
    FIXED,
    PER_MESSAGE_KEY
}