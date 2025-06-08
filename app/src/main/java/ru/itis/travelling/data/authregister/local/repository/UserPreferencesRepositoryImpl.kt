package ru.itis.travelling.data.authregister.local.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import ru.itis.travelling.data.authregister.local.datasource.UserPreferencesDataSource
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource
) : UserPreferencesRepository {

    override suspend fun saveLoginState(isLoggedIn: Boolean, phone: String?) {
        dataSource.saveLoginState(isLoggedIn, phone)
    }

    override suspend fun clearAuthData() {
        dataSource.clearAuthData()
    }

    override val authState: Flow<Pair<Boolean, String?>>
        get() = dataSource.authState

    override suspend fun saveFirebaseToken(token: String) {
        dataSource.saveFirebaseToken(token)
    }

    override suspend fun getFirebaseToken(): String? {
        return dataSource.getFirebaseToken().firstOrNull()
    }

    companion object {
        private const val FIREBASE_TOKEN_KEY = "firebase_token"
    }
}
