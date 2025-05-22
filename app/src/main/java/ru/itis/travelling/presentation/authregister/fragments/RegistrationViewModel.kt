package ru.itis.travelling.presentation.authregister.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.itis.travelling.presentation.authregister.util.ValidationUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.authregister.usecase.RegisterUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.FieldState
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject


@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    val navigator: Navigator
) : ViewModel() {

    private var phoneTouched = false
    private var passwordTouched = false
    private var confirmPasswordTouched = false
    private var submitAttempted = false

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState

    private val _phoneState = MutableStateFlow(FieldState.empty())
    val phoneState: StateFlow<FieldState> = _phoneState

    private val _passwordState = MutableStateFlow(FieldState.empty())
    val passwordState: StateFlow<FieldState> = _passwordState

    private val _confirmPasswordState = MutableStateFlow(FieldState.empty())
    val confirmPasswordState: StateFlow<FieldState> = _confirmPasswordState

    private val _events = MutableSharedFlow<RegistrationEvent>()
    val events: SharedFlow<RegistrationEvent> = _events

    fun onPhoneChanged(rawPhone: String) {
        phoneTouched = true
        val formatted = PhoneNumberUtils.formatPhoneNumber(rawPhone)
        val normalized = PhoneNumberUtils.normalizePhoneNumber(formatted)
        val isValid = ValidationUtils.isValidPhone(normalized)

        _phoneState.value = FieldState(
            value = formatted,
            isValid = isValid,
            shouldShowError = (phoneTouched || submitAttempted) && !isValid
        )
    }

    fun onPasswordChanged(password: String) {
        passwordTouched = true
        val isValid = ValidationUtils.isValidPassword(password)
        _passwordState.value = FieldState(
            value = password,
            isValid = isValid,
            shouldShowError = (passwordTouched || submitAttempted) && !isValid
        )
    }

    fun onConfirmPasswordChanged(password: String) {
        confirmPasswordTouched = true
        _confirmPasswordState.value = FieldState(
            value = password,
            isValid = false,
            shouldShowError = (confirmPasswordTouched || submitAttempted)
        )
        validatePasswordMatch(password)
    }

    private fun validatePasswordMatch(password: String) {
        val passwordsMatch = _passwordState.value.value == _confirmPasswordState.value.value
        _confirmPasswordState.update {
            it.copy(
                value = password,
                isValid = passwordsMatch,
                shouldShowError = (confirmPasswordTouched || submitAttempted) && !passwordsMatch && it.value.isNotEmpty()
            )
        }
    }

    fun register() {
        submitAttempted = true

        _phoneState.update {
            it.copy(shouldShowError = !it.isValid)
        }
        _passwordState.update {
            it.copy(shouldShowError = !it.isValid)
        }
        _confirmPasswordState.update {
            it.copy(shouldShowError = !it.isValid)
        }

        if (!_phoneState.value.isValid ||
            !_passwordState.value.isValid ||
            !_confirmPasswordState.value.isValid) {
            return
        }

        viewModelScope.launch {
            _uiState.update { RegistrationUiState.Loading }
            try {
                val isSuccess = registerUseCase(
                    PhoneNumberUtils.normalizePhoneNumber(_phoneState.value.value),
                    _passwordState.value.value
                )
                if (isSuccess) {
                    _uiState.update { RegistrationUiState.Success }
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
        data object Idle : RegistrationUiState()
        data object Loading : RegistrationUiState()
        data object Success : RegistrationUiState()
    }

    sealed class RegistrationEvent {
        data class ShowError(val error: RegistrationError) : RegistrationEvent()
    }

    enum class RegistrationError {
        UserAlreadyExists,
        Unknown
    }
}