package ru.itis.travelling.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.travelling.R
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl.Companion.DEFAULT_LANGUAGE
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl.Companion.LANGUAGE_KEY
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl.Companion.PREFS_NAME
import ru.itis.travelling.databinding.ActivityMainBinding
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.utils.ThemeUtils
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainContainerId = R.id.main_fragment_container
    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var navigator: Navigator

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val language = prefs.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: DEFAULT_LANGUAGE
        super.attachBaseContext(createLocalizedContext(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!ThemeUtils.isDarkTheme(this)) {
            viewBinding.mainBottomNavigation.setBackgroundColor(getColor(R.color.white))
        }

        initNavigation()
        setupBottomNavigation()
        observeNavigationSelection()
        viewModel.navigateBasedOnAuthState()
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
