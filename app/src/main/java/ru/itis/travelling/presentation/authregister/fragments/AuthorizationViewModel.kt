package ru.itis.travelling.presentation.authregister.fragments

import androidx.fragment.app.FragmentManager
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

    private val _uiState = MutableStateFlow<AuthorizationUiState>(AuthorizationUiState.Idle)
    val uiState: StateFlow<AuthorizationUiState> = _uiState

    private val _events = MutableSharedFlow<AuthorizationEvent>()
    val events: SharedFlow<AuthorizationEvent> = _events

    val authState: StateFlow<AuthState?> = userPreferencesRepository.authState
        .map { (isLoggedIn, phone) -> AuthState(isLoggedIn, phone) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            val normalizedPhone = PhoneNumberUtils.normalizePhoneNumber(phone)
            _uiState.update { AuthorizationUiState.Idle }
            try {
                val isSuccess = loginUseCase(normalizedPhone, password)
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


    fun navigateBasedOnAuthState() {
        viewModelScope.launch {
            authState
                .filterNotNull()
                .first()
                .let { state ->
                    if (!state.isLoggedIn) {
                        navigator.navigateToAuthorizationFragment()
                    } else {
                        requireNotNull(state.userPhone)
                        navigator.navigateToTripsFragment(state.userPhone)
                    }
                }
        }
    }

    fun setUpNavigation(
        mainContainerId: Int,
        rootFragmentManager: FragmentManager,
        onStateChanged: (Navigator.NavigationState) -> Unit
    ) {
        navigator.setUpNavigation(
            mainContainerId = mainContainerId,
            rootFragmentManager = rootFragmentManager,
            stateListener = onStateChanged
        )
    }

    fun onTripsTabSelected() {
        viewModelScope.launch {
            authState.first { it != null }?.userPhone?.let { phone ->
                navigator.navigateToTripsFragment(phone)
            }
        }
    }

    fun onAddTabSelected() {
        viewModelScope.launch {
            authState.first { it != null }?.userPhone?.let { phone ->
                navigator.showAddTripBottomSheet(phone)
            }
        }
    }

    fun onProfileTabSelected() {
        viewModelScope.launch {
            //TODO
            // Handle profile tab navigation
        }
    }

    sealed class AuthorizationUiState {
        object Idle : AuthorizationUiState()
    }

    sealed class AuthorizationEvent {
        data class ShowError(val message: String) : AuthorizationEvent()
    }

    data class AuthState(
        val isLoggedIn: Boolean = false,
        val userPhone: String? = null
    )
}