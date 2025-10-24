package traversium.tripservice.exceptions

class TripNotFoundException(id: Long) : RuntimeException("Trip with ID $id not found")
class TripAlreadyExistsException(id: Long) : RuntimeException("Trip with id '$id' already exists")
class AlbumNotFoundException(id: Long) : RuntimeException("Album with ID $id not found")
class MediaNotFoundException(id: Long) : RuntimeException("Media with ID $id not found")
