package ru.itis.travelling.presentation.authregister.state

sealed class AuthorizationUiState {
    data object Idle : AuthorizationUiState()
    data object Loading : AuthorizationUiState()
    data object Success : AuthorizationUiState()
}