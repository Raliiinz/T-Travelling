package ru.itis.travelling.presentation.authregister.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.authregister.usecase.RegisterUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    val navigator: Navigator
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events: SharedFlow<RegistrationEvent> = _events

    fun register(phone: String, password: String) {
        viewModelScope.launch {
            val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phone)
            _uiState.update { RegistrationUiState.Loading }
            delay(2000)
            try {
                val isSuccess = registerUseCase(normalizedPhone, password)
                if (isSuccess) {
                    navigator.navigateToAuthorizationFragment()
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

    fun navigateToAuthorization() {
        viewModelScope.launch {
            navigator.navigateToAuthorizationFragment()
        }
    }

    sealed class RegistrationUiState {
        object Idle : RegistrationUiState()
        object Loading : RegistrationUiState()
        object Success : RegistrationUiState()
    }

    sealed class RegistrationEvent {
        data class ShowError(val error: RegistrationError) : RegistrationEvent()
    }

    enum class RegistrationError {
        UserAlreadyExists,
        Unknown
    }
}
