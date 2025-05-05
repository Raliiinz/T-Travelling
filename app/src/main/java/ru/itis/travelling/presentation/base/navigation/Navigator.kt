package ru.itis.travelling.presentation.base.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.itis.travelling.R
import ru.itis.travelling.presentation.trips.fragments.AddTripBottomSheet
import ru.itis.travelling.presentation.authregister.fragments.AuthorizationFragment
import ru.itis.travelling.presentation.authregister.fragments.AuthorizationFragment.Companion.AUTHORIZATION_TAG
import ru.itis.travelling.presentation.authregister.fragments.RegistrationFragment
import ru.itis.travelling.presentation.authregister.fragments.RegistrationFragment.Companion.REGISTRATION_TAG
import ru.itis.travelling.presentation.trips.fragments.TripDetailsFragment
import ru.itis.travelling.presentation.trips.fragments.TripDetailsFragment.Companion.TRIP_TAG
import ru.itis.travelling.presentation.trips.fragments.TripsFragment
import ru.itis.travelling.presentation.trips.fragments.TripsFragment.Companion.TRIPS_TAG
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() {

//    sealed class NavigationState {
//        object BottomNavigationHidden : NavigationState()
//        object BottomNavigationVisible : NavigationState()
//    }

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
//            updateNavigationState(NavigationState.BottomNavigationVisible)
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

    fun navigateToEditTrip(tripId: String) {
        //TODO
    }


    fun showAddTripBottomSheet(phone: String) {
        val fragmentManager = requireNotNull(rootFragmentManager.get()) {
            "FragmentManager is not set. Call setUpNavigation() first"
        }

        AddTripBottomSheet.newInstance(phone).show(fragmentManager, AddTripBottomSheet.TAG)
    }



    private fun updateNavigationState(state: NavigationState) {
        navigationStateListener?.invoke(state)
    }

    companion object {
        private const val UNKNOWN_ID = 0
    }
}
