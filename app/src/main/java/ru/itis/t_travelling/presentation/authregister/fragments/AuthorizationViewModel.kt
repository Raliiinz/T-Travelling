package ru.itis.t_travelling.presentation.authregister.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.itis.t_travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.t_travelling.domain.authregister.usecase.LoginUseCase
import javax.inject.Inject

@HiltViewModel
class AuthorizationViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthorizationUiState>(AuthorizationUiState.Idle)
    val uiState: StateFlow<AuthorizationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthorizationEvent>()
    val events: SharedFlow<AuthorizationEvent> = _events.asSharedFlow()

    val authState: StateFlow<AuthState> = userPreferencesRepository.authState
        .map { (isLoggedIn, phone) -> AuthState(isLoggedIn, phone) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState()
        )

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.update { AuthorizationUiState.Loading }

            try {
                val isSuccess = loginUseCase(phone, password)
                if (isSuccess) {
                    userPreferencesRepository.saveLoginState(true, phone)
                    _events.emit(AuthorizationEvent.NavigateToTravelling(phone))
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

    sealed class AuthorizationUiState {
        object Idle : AuthorizationUiState()
        object Loading : AuthorizationUiState()
    }

    sealed class AuthorizationEvent {
        data class NavigateToTravelling(val phone: String) : AuthorizationEvent()
        data class ShowError(val message: String) : AuthorizationEvent()
    }

    data class AuthState(
        val isLoggedIn: Boolean = false,
        val userPhone: String? = null
    )
}