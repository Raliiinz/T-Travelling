package ru.itis.t_travelling.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.usecase.RegisterUseCase
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun register(phone: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
//                if (password != confirmPassword) {
//                    _registrationState.value = RegistrationState.Error(RegistrationError.PasswordMismatch)
//                    return@launch
//                }

                val isSuccess = registerUseCase(phone, password)
                if (isSuccess) {
                    _registrationState.value = RegistrationState.Success
                }
            } catch (e: Exception) {
                when (e.message) {
//                    "Invalid phone number" -> _registrationState.value = RegistrationState.Error(RegistrationError.InvalidPhone)
//                    "Invalid password" -> _registrationState.value = RegistrationState.Error(RegistrationError.InvalidPassword)
                    "User with this phone number already exists" -> _registrationState.value = RegistrationState.Error(RegistrationError.UserAlreadyExists)
                    else -> _registrationState.value = RegistrationState.Error(RegistrationError.Unknown)
                }
            }
        }
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Success : RegistrationState()
        data class Error(val error: RegistrationError) : RegistrationState()
    }

    enum class RegistrationError {
//        InvalidPhone,
//        InvalidPassword,
//        PasswordMismatch,
        UserAlreadyExists,
        Unknown
    }
}