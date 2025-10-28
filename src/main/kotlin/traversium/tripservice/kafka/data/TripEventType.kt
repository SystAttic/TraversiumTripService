package traversium.tripservice.kafka.data

enum class TripEventType {
    TRIP_CREATED,
    TRIP_UPDATED,
    TRIP_DELETED,
    COLLABORATOR_ADDED,
    COLLABORATOR_DELETED,
    VIEWER_ADDED,
    VIEWER_DELETED,
}
