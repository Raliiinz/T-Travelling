package ru.itis.travelling.domain.authregister.repository

import ru.itis.travelling.data.authregister.remote.model.TokensResponse
import ru.itis.travelling.data.network.model.ResultWrapper

interface UserRepository {
    suspend fun registerUser(phone: String, firstName: String, lastName: String, password: String, confirmPassword: String): ResultWrapper<Unit>
    suspend fun login(phone: String, password: String): ResultWrapper<Unit>
    suspend fun refreshTokens(refreshToken: String): ResultWrapper<TokensResponse>
}