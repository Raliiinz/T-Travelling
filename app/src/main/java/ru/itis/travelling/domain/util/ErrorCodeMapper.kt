package ru.itis.travelling.domain.util

import ru.itis.travelling.presentation.common.state.ErrorEvent
import javax.inject.Inject

class ErrorCodeMapper @Inject constructor() {
    fun fromCode(code: Int?): ErrorEvent.FailureReason = when (code) {
        400 -> ErrorEvent.FailureReason.BadRequest
        401 -> ErrorEvent.FailureReason.Unauthorized
        403 -> ErrorEvent.FailureReason.Forbidden
        404 -> ErrorEvent.FailureReason.NotFound
        409 -> ErrorEvent.FailureReason.Conflict
        500 -> ErrorEvent.FailureReason.Server
        else -> ErrorEvent.FailureReason.Unknown
    }
}