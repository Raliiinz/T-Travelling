package ru.itis.travelling.presentation.common.state

data class FieldState(
    val value: String,
    val isValid: Boolean,
    val shouldShowError: Boolean
) {
    companion object {
        fun empty() = FieldState(
            value = "",
            isValid = false,
            shouldShowError = false
        )
    }
}