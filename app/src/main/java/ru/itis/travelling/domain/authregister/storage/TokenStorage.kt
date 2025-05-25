package ru.itis.travelling.domain.authregister.storage

interface TokenStorage {
    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long? = null)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun isAccessTokenExpired(): Boolean
    suspend fun clearTokens()
    suspend fun hasRefreshToken(): Boolean
}