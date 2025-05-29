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

    private val prefsName = "secure_auth_prefs"
    private val keyAccessToken = "access_token"
    private val keyRefreshToken = "refresh_token"
    private val keyExpiresAt = "expires"

    private val mutex = Mutex()

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        prefsName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long?) {
        mutex.withLock {
            prefs.edit {
                putString(keyAccessToken, accessToken)
                putString(keyRefreshToken, refreshToken)
                expiresIn?.let {
                    putLong(keyExpiresAt, System.currentTimeMillis() + it * 1000)
                }
            }
        }
    }

    override suspend fun getAccessToken(): String? {
        return mutex.withLock {
            prefs.getString(keyAccessToken, null)
        }
    }

    override suspend fun getRefreshToken(): String? {
        return mutex.withLock {
            prefs.getString(keyRefreshToken, null)
        }
    }

    override suspend fun isAccessTokenExpired(): Boolean {
        return mutex.withLock {
            val expiresAt = prefs.getLong(keyExpiresAt, 0L)
            System.currentTimeMillis() > (expiresAt - 60_000)
//            expiresAt == 0L || System.currentTimeMillis() > expiresAt
        }
    }

    override suspend fun hasRefreshToken(): Boolean {
        return mutex.withLock {
            !prefs.getString(keyRefreshToken, null).isNullOrBlank()
        }
    }

    override suspend fun clearTokens() {
        mutex.withLock {
            prefs.edit {
                remove(keyAccessToken)
                remove(keyRefreshToken)
                remove(keyExpiresAt)
                apply()
            }
        }
    }
}