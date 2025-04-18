package ru.itis.t_travelling.data.authregister.local.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ru.itis.t_travelling.data.authregister.local.datasource.UserPreferencesDataSource
import ru.itis.t_travelling.domain.authregister.repository.UserPreferencesRepository

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

    override suspend fun isUserLoggedIn(): Boolean {
        return dataSource.authState.first().first
    }

    override suspend fun getLoggedInUserPhone(): String? {
        return dataSource.authState.first().second
    }
}