package ru.itis.travelling.presentation

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.trips.fragments.details.TripDetailsFragment
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val navigator: Navigator
) : ViewModel() {

    private val _selectedNavItemId = MutableStateFlow(DEFAULT_NAV_ITEM_ID)
    val selectedNavItemId: StateFlow<Int> = _selectedNavItemId

    fun onNavItemSelected(itemId: Int) {
        _selectedNavItemId.value = itemId
        when (itemId) {
            R.id.menu_trips_tab -> onTripsTabSelected()
            R.id.menu_add_tab -> onAddTabSelected()
            R.id.menu_profile_tab -> onProfileTabSelected()
        }
    }

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

    private fun onTripsTabSelected() {
        viewModelScope.launch {
            authState.first { it != null }?.userPhone?.let { phone ->
                navigator.navigateToTripsFragment(phone)
            }
        }
    }

    private fun onAddTabSelected() {
        viewModelScope.launch {
            authState.first { it != null }?.userPhone?.let { phone ->
                navigator.showAddTripBottomSheet(phone)
            }
        }
    }

    private fun onProfileTabSelected() {
        viewModelScope.launch {
            navigator.navigateToProfileFragment()
        }
    }

    fun navigateToTripDetails(tripId: String, phone: String, isInvitation: Boolean) {
        navigator.navigateToTripDetailsFragment(tripId, phone, isInvitation)
    }

    data class AuthState(
        val isLoggedIn: Boolean = false,
        val userPhone: String? = null
    )

    companion object {
        val DEFAULT_NAV_ITEM_ID = R.id.menu_trips_tab
    }
}