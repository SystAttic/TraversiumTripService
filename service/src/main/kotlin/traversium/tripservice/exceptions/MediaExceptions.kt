package traversium.tripservice.exceptions

class MediaNotFoundException(id: Long) : RuntimeException("Media with ID $id not found")

class MediaUnauthorizedException(message: String) : RuntimeException(message)