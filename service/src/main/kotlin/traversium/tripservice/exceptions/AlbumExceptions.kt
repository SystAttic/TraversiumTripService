package traversium.tripservice.exceptions

class AlbumNotFoundException(id: Long) : RuntimeException("Album with ID $id not found")
class AlbumWithoutMediaException(id: Long) : RuntimeException("Album with ID $id has no media.")
class AlbumUnauthorizedException(message: String) : RuntimeException(message)
class DatabaseException(message: String) : RuntimeException(message)