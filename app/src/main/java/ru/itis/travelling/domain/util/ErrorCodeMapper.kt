package ru.itis.travelling.domain.util

import ru.itis.travelling.presentation.common.state.ErrorEvent

object ErrorCodeMapper {
    fun fromCode(code: Int?): ErrorEvent.FailureReason = when (code) {
        400 -> ErrorEvent.FailureReason.BadRequest
        401 -> ErrorEvent.FailureReason.Unauthorized
        403 -> ErrorEvent.FailureReason.Forbidden
        404 -> ErrorEvent.FailureReason.NotFound
        500 -> ErrorEvent.FailureReason.Server
        else -> ErrorEvent.FailureReason.Unknown
    }
}