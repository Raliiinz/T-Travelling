package ru.itis.travelling.data.authregister.local.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.itis.travelling.domain.authregister.storage.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class TokenStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenStorage {

    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires"
    }

    private val mutex = Mutex()

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long?) {
        mutex.withLock {
            prefs.edit {
                putString(KEY_ACCESS_TOKEN, accessToken)
                putString(KEY_REFRESH_TOKEN, refreshToken)
                expiresIn?.let {
                    putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + it * 1000)
                }
            }
        }
    }

    override suspend fun getAccessToken(): String? {
        return mutex.withLock {
            prefs.getString(KEY_ACCESS_TOKEN, null)
        }
    }

    override suspend fun getRefreshToken(): String? {
        return mutex.withLock {
            prefs.getString(KEY_REFRESH_TOKEN, null)
        }
    }

    override suspend fun isAccessTokenExpired(): Boolean {
        return mutex.withLock {
            val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
            expiresAt == 0L || System.currentTimeMillis() > expiresAt
        }
    }

    override suspend fun clearTokens() {
        mutex.withLock {
            prefs.edit {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_REFRESH_TOKEN)
                apply()
            }
        }
    }
}