package traversium.tripservice.exceptions

import org.apache.logging.log4j.kotlin.Logging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import traversium.tripservice.dto.ErrorResponse
import java.time.OffsetDateTime

@RestControllerAdvice
class GlobalExceptionHandler : Logging {

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Unexpected error", ex)
        val errorResponse = ErrorResponse(
            message = "Internal Server Error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidJson(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "Invalid request body format",
            status = HttpStatus.BAD_REQUEST.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {

        val message = ex.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        val errorResponse = ErrorResponse(
            message = message.ifBlank { "Validation failed" },
            status = HttpStatus.BAD_REQUEST.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {

        val errorResponse = ErrorResponse(
            message = "Invalid parameter '${ex.name}'",
            status = HttpStatus.BAD_REQUEST.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(
        ex: HttpMediaTypeNotSupportedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {

        val errorResponse = ErrorResponse(
            message = "Unsupported content type",
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(errorResponse)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {

        val errorResponse = ErrorResponse(
            message = "Missing parameter '${ex.parameterName}'",
            status = HttpStatus.BAD_REQUEST.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }



    @ExceptionHandler(ModerationException::class)
    fun handleModerationException(
        ex: ModerationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Moderation failed", ex)
        val status = when (ex.cause) {
            null -> HttpStatus.UNPROCESSABLE_ENTITY
            else -> HttpStatus.SERVICE_UNAVAILABLE
        }
        val errorResponse = ErrorResponse(
            message = "Moderation failed",
            status = status.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(AutosortException::class)
    fun handleAutosortException(
        ex: AutosortException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Autosort failed", ex)
        val errorResponse = ErrorResponse(
            message = "Autosort failed",
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    @ExceptionHandler(DatabaseException::class)
    fun handleDatabaseException(
        ex: DatabaseException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Database error", ex)
        val errorResponse = ErrorResponse(
            message = "Data integrity error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    // Handle 400 Bad Request
    @ExceptionHandler(InvalidDataException::class)
    fun handleInvalidDataException(
        ex: InvalidDataException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Bad request", ex)
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Bad request",
            status = HttpStatus.BAD_REQUEST.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // Handle 403 Forbidden
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(
        ex: UnauthorizedException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Unauthorized", ex)
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Access denied",
            status = HttpStatus.FORBIDDEN.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    // Handle 404 Not Found
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Not found", ex)
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Not found",
            status = HttpStatus.NOT_FOUND.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    // Handle 409 Conflict
    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(
        ex: ConflictException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Conflict", ex)
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Conflict",
            status = HttpStatus.CONFLICT.value(),
            timestamp = OffsetDateTime.now(),
            path = request.getDescription(false).removePrefix("uri=")
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

}
