package traversium.tripservice.exceptions

class AlbumNotFoundException(id: Long) : RuntimeException("Album with ID $id not found")
class AlbumAlreadyExistsException(id: Long) : RuntimeException("Album with ID $id already exists.")
class AlbumWithoutMediaException(id: Long) : RuntimeException("Album with ID $id has no media.")