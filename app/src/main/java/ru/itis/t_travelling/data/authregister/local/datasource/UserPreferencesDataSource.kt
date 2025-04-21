package ru.itis.t_travelling.data.authregister.local.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val context: Context
) {

    private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_PHONE = stringPreferencesKey("user_phone")

    suspend fun saveLoginState(isLoggedIn: Boolean, phone: String?) {
        context.authDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = isLoggedIn
            phone?.let { prefs[USER_PHONE] = it }
        }
    }

    val authState: Flow<Pair<Boolean, String?>> = context.authDataStore.data
        .map { prefs ->
            Pair(
                prefs[IS_LOGGED_IN] ?: false,
                prefs[USER_PHONE]
            )
        }

    suspend fun clearAuthData() {
        context.authDataStore.edit { prefs ->
            prefs.remove(IS_LOGGED_IN)
            prefs.remove(USER_PHONE)
        }
    }
}