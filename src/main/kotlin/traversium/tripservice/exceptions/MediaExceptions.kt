package traversium.tripservice.exceptions

class MediaNotFoundException(id: Long) : RuntimeException("Media with ID $id not found")
