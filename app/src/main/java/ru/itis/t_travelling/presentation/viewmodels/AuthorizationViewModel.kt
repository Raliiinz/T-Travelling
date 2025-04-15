package ru.itis.t_travelling.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.repository.UserPreferencesRepository
import ru.itis.t_travelling.domain.usecase.LoginUseCase
import javax.inject.Inject

@HiltViewModel
class AuthorizationViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            try {
                val isSuccess = loginUseCase(phone, password)
                if (isSuccess) {
                    saveLoginState(true, phone)
                    _loginState.value = LoginState.Success(phone)
                } else {
                    _loginState.value = LoginState.Error("Неверный номер телефона или пароль")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean, email: String?) {
        userPreferencesRepository.saveLoginState(isLoggedIn, email)
    }

    sealed class LoginState {
        object Idle : LoginState()
        data class Success(val phone: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}