package ru.itis.travelling.presentation.base.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.itis.travelling.R
import ru.itis.travelling.presentation.trips.fragments.add.AddTripBottomSheet
import ru.itis.travelling.presentation.authregister.fragments.AuthorizationFragment
import ru.itis.travelling.presentation.authregister.fragments.AuthorizationFragment.Companion.AUTHORIZATION_TAG
import ru.itis.travelling.presentation.authregister.fragments.RegistrationFragment
import ru.itis.travelling.presentation.authregister.fragments.RegistrationFragment.Companion.REGISTRATION_TAG
import ru.itis.travelling.presentation.profile.fragments.ProfileFragment
import ru.itis.travelling.presentation.profile.fragments.ProfileFragment.Companion.PROFILE_TAG
import ru.itis.travelling.presentation.transactions.fragment.TransactionsFragment
import ru.itis.travelling.presentation.transactions.fragment.TransactionsFragment.Companion.TRANSACTIONS_TAG
import ru.itis.travelling.presentation.trips.fragments.details.TripDetailsFragment
import ru.itis.travelling.presentation.trips.fragments.details.TripDetailsFragment.Companion.TRIP_TAG
import ru.itis.travelling.presentation.trips.fragments.overview.TripsFragment
import ru.itis.travelling.presentation.trips.fragments.overview.TripsFragment.Companion.TRIPS_TAG
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() {

    sealed class NavigationState {
        object BottomNavigationHidden : NavigationState()
        data class BottomNavigationVisible(
            val selectedItemId: Int
        ) : NavigationState()
    }

    private var mainContainerId: Int = UNKNOWN_ID
    private lateinit var rootFragmentManager: WeakReference<FragmentManager>
    private var navigationStateListener: ((NavigationState) -> Unit)? = null

    fun setUpNavigation(
        mainContainerId: Int,
        rootFragmentManager: FragmentManager,
        stateListener: (NavigationState) -> Unit
    ) {
        this.mainContainerId = mainContainerId
        this.rootFragmentManager = WeakReference(rootFragmentManager)
        this.navigationStateListener = stateListener
    }

    private fun navigate(
        destination: Fragment,
        destinationTag: String? = null,
        action: NavigationAction,
        isAddToBackStack: Boolean = true,
        backStackTag: String? = null
    ) {
        val fragmentManager = requireNotNull(rootFragmentManager.get()) {
            "FragmentManager is not set. Call setUpNavigation() first"
        }

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.slide_out_right
            )
            .apply {
                when (action) {
                    NavigationAction.ADD -> add(mainContainerId, destination, destinationTag)
                    NavigationAction.REPLACE -> replace(mainContainerId, destination, destinationTag)
                    NavigationAction.REMOVE -> remove(destination)
                }
                if (isAddToBackStack) {
                    addToBackStack(backStackTag)
                }
            }
            .commit()
    }

    fun navigateToAuthorizationFragment() {
        updateNavigationState(NavigationState.BottomNavigationHidden)
        navigate(
            destination = AuthorizationFragment(),
            destinationTag = AUTHORIZATION_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = false
        )
    }

    fun navigateToRegistrationFragment() {
        updateNavigationState(NavigationState.BottomNavigationHidden)
        navigate(
            destination = RegistrationFragment(),
            destinationTag = REGISTRATION_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = true
        )
    }

    fun navigateToTripsFragment(phone: String?) {
        if (phone?.isNotBlank() == true) {
            updateNavigationState(NavigationState.BottomNavigationVisible(R.id.menu_trips_tab))
            navigate(
                destination = TripsFragment.getInstance(param = phone),
                destinationTag = TRIPS_TAG,
                action = NavigationAction.REPLACE,
                isAddToBackStack = false
            )
        } else {
            navigateToAuthorizationFragment()
        }
    }

    fun navigateToTripDetailsFragment(tripId: String, phoneNumber: String) {
        updateNavigationState(NavigationState.BottomNavigationHidden)
        navigate(
            destination = TripDetailsFragment.getInstance(tripId, phoneNumber),
            destinationTag = TRIP_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = true
        )
    }

    fun showAddTripBottomSheet(phone: String, tripId: String) {
        val fragmentManager = requireNotNull(rootFragmentManager.get()) {
            "FragmentManager is not set. Call setUpNavigation() first"
        }

        AddTripBottomSheet.newInstanceForEditing(phone, tripId).show(fragmentManager, AddTripBottomSheet.TAG)
    }

    fun showAddTripBottomSheet(phoneNumber: String) {
        val fragmentManager = requireNotNull(rootFragmentManager.get()) {
            "FragmentManager is not set. Call setUpNavigation() first"
        }

        AddTripBottomSheet.newInstance(phoneNumber).show(fragmentManager, AddTripBottomSheet.TAG)
    }

    private fun updateNavigationState(state: NavigationState) {
        navigationStateListener?.invoke(state)
    }

    fun navigateToProfileFragment() {
        updateNavigationState(NavigationState.BottomNavigationVisible(R.id.menu_profile_tab))
        navigate(
            destination = ProfileFragment(),
            destinationTag = PROFILE_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = false
        )
    }

    fun navigateToTransactionsFragment(tripId: String, phone: String) {
        updateNavigationState(NavigationState.BottomNavigationHidden)
        navigate(
            destination = TransactionsFragment.getInstance(tripId, phone),
            destinationTag = TRANSACTIONS_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = true
        )
    }

    companion object {
        private const val UNKNOWN_ID = 0
    }
}
