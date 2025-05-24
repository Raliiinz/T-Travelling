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
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.model.User
import ru.itis.travelling.domain.authregister.usecase.RegisterUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.authregister.state.RegistrationUiState
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.FieldState
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject


@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    val navigator: Navigator
) : ViewModel() {

    private var firstNameTouched = false
    private var lastNameTouched = false
    private var phoneTouched = false
    private var passwordTouched = false
    private var confirmPasswordTouched = false
    private var submitAttempted = false

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState

    private val _firstNameState = MutableStateFlow(FieldState.empty())
    val firstNameState: StateFlow<FieldState> = _firstNameState

    private val _lastNameState = MutableStateFlow(FieldState.empty())
    val lastNameState: StateFlow<FieldState> = _lastNameState

    private val _phoneState = MutableStateFlow(FieldState.empty())
    val phoneState: StateFlow<FieldState> = _phoneState

    private val _passwordState = MutableStateFlow(FieldState.empty())
    val passwordState: StateFlow<FieldState> = _passwordState

    private val _confirmPasswordState = MutableStateFlow(FieldState.empty())
    val confirmPasswordState: StateFlow<FieldState> = _confirmPasswordState

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

    fun onFirstNameChanged(firstName: String) {
        firstNameTouched = true
        val isValid = ValidationUtils.isValidName(firstName)
        _firstNameState.value = FieldState(
            value = firstName,
            isValid = isValid,
            shouldShowError = (firstNameTouched || submitAttempted) && !isValid
        )
    }

    fun onLastNameChanged(lastName: String) {
        lastNameTouched = true
        val isValid = ValidationUtils.isValidName(lastName)
        _lastNameState.value = FieldState(
            value = lastName,
            isValid = isValid,
            shouldShowError = (lastNameTouched || submitAttempted) && !isValid
        )
    }

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

        listOf(
            _firstNameState,
            _lastNameState,
            _phoneState,
            _passwordState,
            _confirmPasswordState
        ).forEach { state ->
            state.update { it.copy(shouldShowError = !it.isValid) }
        }

        if (!_firstNameState.value.isValid ||
            !_lastNameState.value.isValid ||
            !_phoneState.value.isValid ||
            !_passwordState.value.isValid ||
            !_confirmPasswordState.value.isValid) {
            return
        }

        viewModelScope.launch {
            _uiState.update { RegistrationUiState.Loading }

            val user = User(
                phoneNumber = PhoneNumberUtils.normalizePhoneNumber(_phoneState.value.value),
                firstName = _firstNameState.value.value,
                lastName = _lastNameState.value.value,
                password = _passwordState.value.value,
                confirmPassword = _confirmPasswordState.value.value
            )

            when (val result = registerUseCase(user)) {
                is ResultWrapper.Success -> {
                    _uiState.update { RegistrationUiState.Success }
                    navigator.navigateToAuthorizationFragment()
                }
                is ResultWrapper.GenericError -> {
                    handleRegistrationError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.FullError(
                        R.string.error_title_network,
                        R.string.error_network
                    ))
                }
            }

            _uiState.update { RegistrationUiState.Idle }
        }
    }

    private suspend fun handleRegistrationError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)

        val errorEvent = when (reason) {
            ErrorEvent.FailureReason.BadRequest -> ErrorEvent.FullError(
                R.string.error_title_validation_registration,
                R.string.error_bad_request_registration
            )
            ErrorEvent.FailureReason.Server -> ErrorEvent.FullError(
                R.string.error_title_server,
                R.string.error_server
            )
            ErrorEvent.FailureReason.Network -> ErrorEvent.FullError(
                R.string.error_title_network,
                R.string.error_network
            )
            else -> ErrorEvent.FullError(
                R.string.error_title_unknown,
                R.string.error_unknown
            )
        }

        _errorEvent.emit(errorEvent)
    }

    fun navigateToAuthorization() {
        viewModelScope.launch {
            navigator.navigateToAuthorizationFragment()
        }
    }
}