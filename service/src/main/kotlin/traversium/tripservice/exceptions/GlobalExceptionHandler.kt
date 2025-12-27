package traversium.tripservice.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TripModerationException::class)
    fun handleModeration(ex: TripModerationException): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "error" to "MODERATION_FAILED",
                    "message" to ex.message.orEmpty()
                )
            )

    @ExceptionHandler(AlbumModerationException::class)
    fun handleModeration(ex: AlbumModerationException): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "error" to "MODERATION_FAILED",
                    "message" to ex.message.orEmpty()
                )
            )

    @ExceptionHandler(InvalidTripException::class)
    fun handleInvalidTrip(ex: InvalidTripException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to ex.message.orEmpty()))
    }

    @ExceptionHandler(AutosortException::class)
    fun handleAutosort(ex: AutosortException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(mapOf("error" to ex.message.orEmpty()))
    }

}
