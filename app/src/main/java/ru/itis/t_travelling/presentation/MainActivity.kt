package ru.itis.t_travelling.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

//        lifecycleScope.launchWhenCreated {
//            // Получаем состояние СИНХРОННО
//            val (isLoggedIn, phone) = viewModel.authState.value
//
//            setContentView(R.layout.activity_main)
//            initNavigation()
//
//            if (isLoggedIn && !phone.isNullOrEmpty()) {
//                navigator.navigateToTravellingFragment(phone)
//            } else {
//                navigator.navigateToAuthorizationFragment()
//            }
//        }

        initNavigation()
        observeAuthState()
        setupBottomNavigation()
    }

    private fun initNavigation() {
        navigator.setUpNavigation(
            mainContainerId = mainContainerId,
            rootFragmentManager = supportFragmentManager,
            stateListener = { state ->
                when (state) {
                    Navigator.NavigationState.BottomNavigationHidden -> hideBottomNavigation()
                    Navigator.NavigationState.BottomNavigationVisible -> showBottomNavigation()
                }
            }
        )
    }

    private fun observeAuthState() {
        viewModel.authState
            .onEach { state ->
                when {
                    !state.isLoggedIn -> navigator.navigateToAuthorizationFragment()
                    !state.userPhone.isNullOrEmpty() -> navigator.navigateToTravellingFragment(state.userPhone)
                }
            }
            .launchIn(lifecycleScope)
    }

    fun setupBottomNavigation() {
        viewBinding.mainBottomNavigation.apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.menu_trips_tab -> {
                        val currentPhone = viewModel.authState.value.userPhone
                        if (currentPhone?.isNotBlank() == true) {
                            navigator.navigateToTravellingFragment(currentPhone)
                        }
                        true
                    }

                    R.id.menu_add_tab -> {
                        // TODO: Добавить обработку для menu_other_tab
                        true
                    }

                    R.id.menu_profile_tab -> {
                        // TODO: Добавить обработку для menu_settings_tab
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
}