package ru.itis.travelling.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.travelling.R
import ru.itis.travelling.databinding.ActivityMainBinding
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.authregister.fragments.AuthorizationViewModel
import ru.itis.travelling.presentation.trips.util.PermissionHandler
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainContainerId = R.id.main_fragment_container
    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val viewModel: AuthorizationViewModel by viewModels()
    var permissionHandler: PermissionHandler? = null
    @Inject lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionHandler = PermissionHandler(
            onSinglePermissionGranted = {},
            onSinglePermissionDenied = {}
        )
        permissionHandler?.initContracts(this)

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
                    is Navigator.NavigationState.BottomNavigationHidden -> {
                        hideBottomNavigation()
                    }
                    is Navigator.NavigationState.BottomNavigationVisible -> {
                        showBottomNavigation()
                        viewBinding.mainBottomNavigation.selectedItemId = state.selectedItemId
                    }
                }
            }
//            onStateChanged = { state ->
//                when (state) {
//                    Navigator.NavigationState.BottomNavigationHidden -> hideBottomNavigation()
//                    Navigator.NavigationState.BottomNavigationVisible -> showBottomNavigation()
//                }
//            }
        )
    }

    private fun setupBottomNavigation() {
        viewBinding.mainBottomNavigation.apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menu_trips_tab -> {
                        viewModel.onTripsTabSelected()
                        menu.findItem(R.id.menu_trips_tab).isChecked = true
                        true
                    }
                    R.id.menu_add_tab -> {
                        viewModel.onAddTabSelected()
                        menu.findItem(R.id.menu_add_tab).isChecked = true
                        true
                    }
                    R.id.menu_archive_tab -> {
                        viewModel.onProfileTabSelected()
                        menu.findItem(R.id.menu_archive_tab).isChecked = true
                        true
                    }
                    else -> false
                }
            }
            setOnItemReselectedListener {}
        }
    }

    fun showBottomNavigation() {
        viewBinding.mainBottomNavigation.visibility = View.VISIBLE
    }

    fun hideBottomNavigation() {
        viewBinding.mainBottomNavigation.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionHandler = null
    }
}
