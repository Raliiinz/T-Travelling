package ru.itis.travelling.presentation.authregister.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.authregister.usecase.LoginUseCase
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject

@HiltViewModel
class AuthorizationViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val loginUseCase: LoginUseCase,
    private val navigator: Navigator
) : ViewModel() {

    private var phoneTouched = false
    private var passwordTouched = false
    private var submitAttempted = false

    private val _uiState = MutableStateFlow<AuthorizationUiState>(AuthorizationUiState.Idle)
    val uiState: StateFlow<AuthorizationUiState> = _uiState

    private val _phoneState = MutableStateFlow(FieldState("", false, shouldShowError = false))
    val phoneState: StateFlow<FieldState> = _phoneState

    private val _passwordState = MutableStateFlow(FieldState("", false, shouldShowError = false))
    val passwordState: StateFlow<FieldState> = _passwordState

    private val _events = MutableSharedFlow<AuthorizationEvent>()
    val events: SharedFlow<AuthorizationEvent> = _events

    fun onPhoneChanged(rawPhone: String) {
        phoneTouched = true
        val formatted = PhoneNumberUtils.formatPhoneNumber(rawPhone)
        val isValid = formatted.isNotBlank()

        _phoneState.value = FieldState(
            value = formatted,
            isValid = isValid,
            shouldShowError = (phoneTouched || submitAttempted) && !isValid
        )
    }

    fun onPasswordChanged(password: String) {
        passwordTouched = true
        val isValid = password.isNotBlank()

        _passwordState.value = FieldState(
            value = password,
            isValid = isValid,
            shouldShowError = (passwordTouched || submitAttempted) && !isValid
        )
    }

    fun login() {
        submitAttempted = true

        _phoneState.update {
            it.copy(shouldShowError = !it.isValid)
        }
        _passwordState.update {
            it.copy(shouldShowError = !it.isValid)
        }

        if (!_phoneState.value.isValid || !_passwordState.value.isValid) {
            return
        }

        viewModelScope.launch {
            val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(_phoneState.value.value)
            _uiState.update { AuthorizationUiState.Loading }
            try {
                val isSuccess = loginUseCase(
                    normalizedPhone,
                    _passwordState.value.value
                )
                if (isSuccess) {
                    userPreferencesRepository.saveLoginState(true, normalizedPhone)
                    navigator.navigateToTripsFragment(normalizedPhone)
                } else {
                    _events.emit(AuthorizationEvent.ShowError("Неверный номер телефона или пароль"))
                }
            } catch (e: Exception) {
                _events.emit(AuthorizationEvent.ShowError(e.message ?: "Ошибка"))
            } finally {
                _uiState.update { AuthorizationUiState.Idle }
            }
        }
    }

    fun navigateToRegistration() {
        viewModelScope.launch {
            navigator.navigateToRegistrationFragment()
        }
    }

    data class FieldState(
        val value: String,
        val isValid: Boolean,
        val shouldShowError: Boolean
    )

    sealed class AuthorizationUiState {
        data object Idle : AuthorizationUiState()
        data object Loading : AuthorizationUiState()
        data object Success : AuthorizationUiState()
    }

    sealed class AuthorizationEvent {
        data class ShowError(val message: String) : AuthorizationEvent()
    }
}