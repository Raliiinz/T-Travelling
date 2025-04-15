package ru.itis.t_travelling.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.itis.t_travelling.domain.repository.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun saveLoginState(isLoggedIn: Boolean, email: String?) {
        userPreferencesRepository.saveLoginState(isLoggedIn, email)
    }

    fun isUserLoggedIn(): Boolean {
        return userPreferencesRepository.isUserLoggedIn()
    }

    fun getLoggedInUserPhone(): String? {
        return userPreferencesRepository.getLoggedInUserPhone()
    }
}