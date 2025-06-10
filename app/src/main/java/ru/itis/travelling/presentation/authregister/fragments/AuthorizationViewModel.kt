package ru.itis.travelling.presentation.authregister.fragments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.authregister.usecase.LoginUseCase
import ru.itis.travelling.domain.profile.usecase.UpdateDeviceTokenUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
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
    private val updateDeviceTokenUseCase: UpdateDeviceTokenUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
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
        val shouldShow = (phoneTouched || submitAttempted) && !isValid
        val errorRes = if (shouldShow) R.string.error_phone_empty else null

        _phoneState.value = FieldState(
            value = formatted,
            isValid = isValid,
            shouldShowError = shouldShow,
            errorMessageRes = errorRes
        )
    }

    fun onPasswordChanged(password: String) {
        passwordTouched = true
        val isValid = password.isNotBlank()
        val shouldShow = (passwordTouched || submitAttempted) && !isValid
        val errorRes = if (shouldShow) R.string.error_password_empty else null

        _passwordState.value = FieldState(
            value = password,
            isValid = isValid,
            shouldShowError = shouldShow,
            errorMessageRes = errorRes
        )
    }

    fun login() {
        submitAttempted = true

        _phoneState.update {
            val shouldShow = !it.isValid
            val errorRes = if (shouldShow) R.string.error_phone_empty else null
            it.copy(
                shouldShowError = shouldShow,
                errorMessageRes = errorRes
            )
        }

        _passwordState.update {
            val shouldShow = !it.isValid
            val errorRes = if (shouldShow) R.string.error_password_empty else null
            it.copy(
                shouldShowError = shouldShow,
                errorMessageRes = errorRes
            )
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
                    sendDeviceTokenIfAvailable().join()
                    navigator.navigateToTripsFragment(normalizedPhone)
                }
                is ResultWrapper.GenericError -> {
                    handleRegistrationError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }

            _uiState.update { AuthorizationUiState.Idle }
        }
    }

    private fun sendDeviceTokenIfAvailable(): Job = viewModelScope.launch {
        try {
            userPreferencesRepository.getFirebaseToken()?.let { token ->
                println("Sending token: $token") // Добавьте логирование
                when (updateDeviceTokenUseCase(token)) {
                    is ResultWrapper.Success -> Log.d("AuthVM", "Token updated")
                    else -> Log.e("AuthVM", "Error updating token")
                }
            } ?: run {
                Log.e("AuthVM", "Firebase token is null") // Важно для диагностики
            }
        } catch (e: Exception) {
            Log.e("AuthVM", "Error sending device token", e)
        }
    }

//    private fun sendDeviceTokenIfAvailable() {
//        viewModelScope.launch {
//            try {
//                userPreferencesRepository.getFirebaseToken()?.let { token ->
//                    println(userPreferencesRepository.getFirebaseToken())
//                    when (updateDeviceTokenUseCase(token)) {
//                        is ResultWrapper.Success -> Log.d("AuthVM", "Token updated")
//                        is ResultWrapper.NetworkError -> Log.e("AuthVM", "Network error updating token")
//                        is ResultWrapper.GenericError -> Log.e("AuthVM", "Server error updating token")
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("AuthVM", "Error sending device token", e)
//            }
//        }
//    }

    private suspend fun handleRegistrationError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_authorization
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    fun navigateToRegistration() {
        viewModelScope.launch {
            navigator.navigateToRegistrationFragment()
        }
    }
}