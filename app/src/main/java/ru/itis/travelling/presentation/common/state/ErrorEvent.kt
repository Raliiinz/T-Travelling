package ru.itis.travelling.presentation.common.state

import androidx.annotation.StringRes

sealed class ErrorEvent {

    data class FullError(
        @StringRes val titleRes: Int,
        @StringRes val messageRes: Int
    ) : ErrorEvent()

    data class MessageOnly(
        @StringRes val messageRes: Int
    ) : ErrorEvent()

    enum class FailureReason {
        Unauthorized,
        Forbidden,
        NotFound,
        BadRequest,
        Conflict,
        Server,
        Network,
        Unknown
    }
}