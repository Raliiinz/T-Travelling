package ru.itis.travelling.domain.authregister.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun saveLoginState(isLoggedIn: Boolean, phone: String?)
    suspend fun clearAuthData()
    val authState: Flow<Pair<Boolean, String?>>
    suspend fun saveFirebaseToken(token: String)
    suspend fun getFirebaseToken(): String?
}
