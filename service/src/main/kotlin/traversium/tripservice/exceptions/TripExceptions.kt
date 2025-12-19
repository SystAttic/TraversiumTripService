package traversium.tripservice.exceptions

class TripNotFoundException(id: Long) : RuntimeException("Trip with ID $id not found")
class TripAlreadyExistsException(id: Long) : RuntimeException("Trip with ID '$id' already exists")
class TripWithoutAlbumsException(id: Long) : RuntimeException("Trip with ID '$id' has no albums")

class InvalidTripDataException() : RuntimeException("Invalid trip data")
class TripWithoutCollaboratorException(tripId: Long, collaboratorId: String) : RuntimeException("Trip $tripId has no collaborator $collaboratorId.")
class TripHasCollaboratorException(collaboratorId: String) : RuntimeException("Collaborator with ID '$collaboratorId' already exists in trip.")

class TripWithoutViewerException(tripId: Long, viewerId: String) : RuntimeException("Trip $tripId has no viewer $viewerId.")
class TripHasViewerException(viewerId: String) : RuntimeException("Viewer with ID '$viewerId' already exists in trip.")

class TripUnauthorizedException(message: String) : RuntimeException(message)
class TripModerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)