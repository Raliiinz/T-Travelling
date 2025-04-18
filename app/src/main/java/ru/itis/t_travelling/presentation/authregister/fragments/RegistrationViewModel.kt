package ru.itis.t_travelling.presentation.authregister.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.authregister.usecase.RegisterUseCase
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events: SharedFlow<RegistrationEvent> = _events.asSharedFlow()

    fun register(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.update { RegistrationUiState.Loading }

            try {
                val isSuccess = registerUseCase(phone, password)
                if (isSuccess) {
                    _events.emit(RegistrationEvent.NavigateToAuthorization)
                }
            } catch (e: Exception) {
                val error = when (e.message) {
                    "User with this phone number already exists" ->
                        RegistrationError.UserAlreadyExists
                    else -> RegistrationError.Unknown
                }
                _events.emit(RegistrationEvent.ShowError(error))
            } finally {
                _uiState.update { RegistrationUiState.Idle }
            }
        }
    }

    sealed class RegistrationUiState {
        object Idle : RegistrationUiState()
        object Loading : RegistrationUiState()
    }

    sealed class RegistrationEvent {
        object NavigateToAuthorization : RegistrationEvent()
        data class ShowError(val error: RegistrationError) : RegistrationEvent()
    }

    enum class RegistrationError {
        UserAlreadyExists,
        Unknown
    }
}