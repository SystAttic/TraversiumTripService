package traversium.tripservice.dto

import java.time.OffsetDateTime

data class ErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val path: String? = null
)