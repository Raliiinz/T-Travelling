package ru.itis.travelling.presentation.authregister.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.authregister.usecase.LoginUseCase
import ru.itis.travelling.presentation.authregister.state.AuthorizationUiState
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.common.state.FieldState
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

    private val _phoneState = MutableStateFlow(FieldState.empty())
    val phoneState: StateFlow<FieldState> = _phoneState

    private val _passwordState = MutableStateFlow(FieldState.empty())
    val passwordState: StateFlow<FieldState> = _passwordState

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

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
            _uiState.update { AuthorizationUiState.Loading }

            val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(_phoneState.value.value)
            val result = loginUseCase(normalizedPhone, _passwordState.value.value)

            when (result) {
                is ResultWrapper.Success -> {
                    userPreferencesRepository.saveLoginState(true, normalizedPhone)
                    navigator.navigateToTripsFragment(normalizedPhone)
                }
                is ResultWrapper.GenericError -> {
                    handleRegistrationError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.Error(ErrorEvent.FailureReason.Network))
                }
            }

            _uiState.update { AuthorizationUiState.Idle }
        }
    }

    private suspend fun handleRegistrationError(code: Int?) {
        val errorReason = when (code) {
            400 -> ErrorEvent.FailureReason.BadRequest
            401 -> ErrorEvent.FailureReason.Unauthorized
            403 -> ErrorEvent.FailureReason.Forbidden
            404 -> ErrorEvent.FailureReason.NotFound
            500 -> ErrorEvent.FailureReason.Server
            else -> ErrorEvent.FailureReason.Unknown
        }
        _errorEvent.emit(ErrorEvent.Error(errorReason))
    }

    fun navigateToRegistration() {
        viewModelScope.launch {
            navigator.navigateToRegistrationFragment()
        }
    }
}