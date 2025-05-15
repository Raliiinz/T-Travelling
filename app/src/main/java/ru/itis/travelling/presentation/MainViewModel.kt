package ru.itis.travelling.presentation

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.presentation.base.navigation.Navigator
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val navigator: Navigator
) : ViewModel() {

    val authState: StateFlow<AuthState?> = userPreferencesRepository.authState
        .map { (isLoggedIn, phone) -> AuthState(isLoggedIn, phone) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

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

    data class AuthState(
        val isLoggedIn: Boolean = false,
        val userPhone: String? = null
    )
}