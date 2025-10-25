package traversium.tripservice.exceptions

class AlbumNotFoundException(id: Long) : RuntimeException("Album with ID $id not found")
