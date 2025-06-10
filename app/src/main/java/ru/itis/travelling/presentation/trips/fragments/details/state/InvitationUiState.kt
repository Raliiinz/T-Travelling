package ru.itis.travelling.presentation.trips.fragments.details.state

import androidx.annotation.StringRes

sealed class InvitationUiState {
    object Idle : InvitationUiState()
    object Loading : InvitationUiState()
    data class Success(
        @StringRes val message: Int,
        val shouldHideActions: Boolean = false
    ) : InvitationUiState()
    data class Error(
        val message: Any,
        val errorCode: Int? = null
    ) : InvitationUiState()
}