package ru.itis.travelling.presentation.authregister.state

sealed class RegistrationUiState {
    data object Idle : RegistrationUiState()
    data object Loading : RegistrationUiState()
    data object Success : RegistrationUiState()
}