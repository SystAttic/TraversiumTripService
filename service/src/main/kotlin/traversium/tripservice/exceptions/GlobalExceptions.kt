package traversium.tripservice.exceptions

// 400 - bad request
class InvalidDataException(message: String) : RuntimeException(message)
// 403 - forbidden
class UnauthorizedException(message: String) : RuntimeException(message)
// 404 - not found
class NotFoundException(message: String) : RuntimeException(message)
// 409 - conflict
class ConflictException(message: String) : RuntimeException(message)
// 500 - internal server error
class DatabaseException(message: String) : RuntimeException(message)

// Moderation service
class ModerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

// Trip Media autosort
class AutosortException(message: String) : RuntimeException(message)

