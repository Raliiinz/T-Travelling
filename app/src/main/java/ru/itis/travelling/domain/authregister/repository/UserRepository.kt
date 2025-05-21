package ru.itis.travelling.domain.authregister.repository

import ru.itis.travelling.data.authregister.remote.model.TokensResponse

interface UserRepository {
    suspend fun registerUser(phone: String, firstName: String, lastName: String, password: String, confirmPassword: String)
    suspend fun login(phone: String, password: String)
    suspend fun refreshTokens(refreshToken: String): TokensResponse
}
