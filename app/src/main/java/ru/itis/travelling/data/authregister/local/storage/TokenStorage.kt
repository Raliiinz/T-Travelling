package ru.itis.travelling.data.authregister.local.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}

@Singleton
class SecureTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenStorage {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        mutex.withLock {
            prefs.edit() {
                putString("access_token", accessToken)
                    .putString("refresh_token", refreshToken)
            }
        }
    }

    override suspend fun getAccessToken(): String? {
        return mutex.withLock {
            prefs.getString("access_token", null)
        }
    }

    override suspend fun getRefreshToken(): String? {
        return mutex.withLock {
            prefs.getString("refresh_token", null)
        }
    }

    override suspend fun clearTokens() {
        mutex.withLock {
            prefs.edit() { clear() }
        }
    }
}