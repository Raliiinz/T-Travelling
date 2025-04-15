package ru.itis.t_travelling.domain.repository

interface UserPreferencesRepository {
    fun saveLoginState(isLoggedIn: Boolean, phone: String?)
    fun isUserLoggedIn(): Boolean
    fun getLoggedInUserPhone(): String?
}