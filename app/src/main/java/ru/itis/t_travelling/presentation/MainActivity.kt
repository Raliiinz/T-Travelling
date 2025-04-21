package ru.itis.t_travelling.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.ActivityMainBinding
import ru.itis.t_travelling.presentation.base.navigation.Navigator
import ru.itis.t_travelling.presentation.authregister.fragments.AuthorizationViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainContainerId = R.id.main_fragment_container
    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val viewModel: AuthorizationViewModel by viewModels()
    @Inject lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNavigation()
        setupBottomNavigation()
        viewModel.navigateBasedOnAuthState()
    }

    private fun initNavigation() {
        viewModel.setUpNavigation(
            mainContainerId = mainContainerId,
            rootFragmentManager = supportFragmentManager,
            onStateChanged = { state ->
                when (state) {
                    Navigator.NavigationState.BottomNavigationHidden -> hideBottomNavigation()
                    Navigator.NavigationState.BottomNavigationVisible -> showBottomNavigation()
                }
            }
        )
    }

    private fun setupBottomNavigation() {
        viewBinding.mainBottomNavigation.apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menu_trips_tab -> {
                        viewModel.onTripsTabSelected()
                        true
                    }
                    R.id.menu_add_tab -> {
                        viewModel.onAddTabSelected()
                        true
                    }
                    R.id.menu_profile_tab -> {
                        viewModel.onProfileTabSelected()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    fun showBottomNavigation() {
        viewBinding.mainBottomNavigation.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        viewBinding.mainBottomNavigation.visibility = View.GONE
    }
}