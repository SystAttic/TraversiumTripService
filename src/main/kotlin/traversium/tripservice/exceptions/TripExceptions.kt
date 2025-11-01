package traversium.tripservice.exceptions

class TripNotFoundException(id: Long) : RuntimeException("Trip with ID $id not found")
class TripAlreadyExistsException(id: Long) : RuntimeException("Trip with ID '$id' already exists")
class TripWithoutAlbumsException(id: Long) : RuntimeException("Trip with ID '$id' has no albums")

class InvalidTripDataException() : RuntimeException("Invalid trip data")
class TripWithoutCollaboratorException(tripId: Long, collaboratorId: String) : RuntimeException("Trip $tripId has no collaborator $collaboratorId.")
class TripHasCollaboratorException(tripId: Long, collaboratorId: String) : RuntimeException("Collaborator with ID '$collaboratorId' already exists in trip with ID $tripId.")

class TripWithoutViewerException(tripId: Long, viewerId: String) : RuntimeException("Trip $tripId has no viewer $viewerId.")
class TripHasViewerException(tripId: Long, viewerId: String) : RuntimeException("Viewer with ID '$viewerId' already exists in trip with ID $tripId.")