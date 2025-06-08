package ru.itis.travelling.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.travelling.R
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl.Companion.DEFAULT_LANGUAGE
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl.Companion.LANGUAGE_KEY
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl.Companion.PREFS_NAME
import ru.itis.travelling.databinding.ActivityMainBinding
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.trips.fragments.details.TripDetailsFragment
import ru.itis.travelling.presentation.utils.PermissionsHandler
import ru.itis.travelling.presentation.utils.ThemeUtils
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainContainerId = R.id.main_fragment_container
    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var navigator: Navigator
    private var permissionsHandler: PermissionsHandler? = null


    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language = prefs.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: DEFAULT_LANGUAGE
        super.attachBaseContext(createLocalizedContext(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsHandler?.requestSinglePermission(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        handleNotificationIntent(intent)

        if (!ThemeUtils.isDarkTheme(this)) {
            viewBinding.mainBottomNavigation.setBackgroundColor(getColor(R.color.white))
        }

        initNavigation()
        setupBottomNavigation()
        observeNavigationSelection()
        viewModel.navigateBasedOnAuthState()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.extras?.let { extras ->
            when (extras.getString("NAVIGATE_TO")) {
                "trip_details" -> {
                    val tripId = extras.getString("TRIP_ID") ?: return
                    val phone = extras.getString("PHONE_TEXT") ?: return
                    navigateToTripDetails(tripId, phone)
                }
            }
        }
    }

    private fun navigateToTripDetails(tripId: String, phone: String) {
        // Навигация к фрагменту деталей поездки
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container,
                TripDetailsFragment.getInstance(tripId, phone))
            .addToBackStack(null)
            .commit()
    }

    private fun observeNavigationSelection() {
        lifecycleScope.launchWhenStarted {
            viewModel.selectedNavItemId.collect { itemId ->
                if (viewBinding.mainBottomNavigation.selectedItemId != itemId) {
                    viewBinding.mainBottomNavigation.selectedItemId = itemId
                }
            }
        }
    }

    private fun createLocalizedContext(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration().apply {
            setLocale(locale)
        }

        return context.createConfigurationContext(config)
    }

    fun changeLanguage() {
        recreate()
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
        )
    }

    private fun setupBottomNavigation() {
        viewBinding.mainBottomNavigation.apply {
            setOnItemSelectedListener { item ->
                viewModel.onNavItemSelected(item.itemId)
                true
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
