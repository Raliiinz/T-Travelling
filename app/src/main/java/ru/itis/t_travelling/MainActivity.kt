package ru.itis.t_travelling

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.t_travelling.databinding.ActivityMainBinding
import ru.itis.t_travelling.presentation.base.BaseActivity
import ru.itis.t_travelling.presentation.base.NavigationAction
import ru.itis.t_travelling.presentation.fragments.AuthorizationFragment
import ru.itis.t_travelling.presentation.fragments.TravellingFragment
import ru.itis.t_travelling.presentation.viewmodels.MainViewModel

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override val mainContainerId = R.id.main_fragment_container
    private var viewBinding: ActivityMainBinding? = null

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        navigateToAppropriateFragment()


        viewBinding?.mainBottomNavigation?.setOnItemReselectedListener {}

    }

    private fun navigateToAppropriateFragment() {
        if (viewModel.isUserLoggedIn()) {
            val userPhone = viewModel.getLoggedInUserPhone()
            navigateToTravellingFragment(userPhone)
        } else {
            navigateToAuthorizationFragment()
        }
    }

    private fun navigateToAuthorizationFragment() {
        hideBottomNavigation()
        navigate(
            destination = AuthorizationFragment(),
            destinationTag = AuthorizationFragment.Companion.AUTHORIZATION_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = false
        )
    }

    private fun navigateToTravellingFragment(phone: String?) {
        if (!phone.isNullOrEmpty()) {
            setupBottomNavigation()
            showBottomNavigation()
            navigate(
                destination = TravellingFragment.Companion.getInstance(param = phone),
                destinationTag = TravellingFragment.Companion.TRAVELLING_TAG,
                action = NavigationAction.REPLACE,
                isAddToBackStack = false
            )
        } else {
            navigateToAuthorizationFragment()
        }

    }

    fun setupBottomNavigation() {
        val bottomNavigationView = viewBinding?.mainBottomNavigation
        bottomNavigationView?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_trips_tab -> {
                    val userPhone = viewModel.getLoggedInUserPhone()
                    if (!userPhone.isNullOrEmpty()) {
                        navigate(
                            destination = TravellingFragment.Companion.getInstance(param = userPhone),
                            destinationTag = TravellingFragment.Companion.TRAVELLING_TAG,
                            action = NavigationAction.REPLACE,
                            isAddToBackStack = false
                        )
                    }
                    true
                }
                else -> false
            }
        }
    }


    fun hideBottomNavigation() {
        viewBinding?.mainBottomNavigation?.visibility = View.GONE
    }

    fun showBottomNavigation() {
        viewBinding?.mainBottomNavigation?.visibility = View.VISIBLE
    }


    override fun onDestroy() {
        viewBinding = null
        super.onDestroy()
    }
}