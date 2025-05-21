package ru.itis.travelling.presentation.common.state

sealed class ErrorEvent {
    data class Error(val reason: FailureReason) : ErrorEvent()

    enum class FailureReason {
        Unauthorized,
        Forbidden,
        NotFound,
        BadRequest,
        Server,
        Network,
        Unknown,
        Success
    }
}