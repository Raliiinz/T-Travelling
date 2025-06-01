package ru.itis.travelling.data.authregister.local.repository

import ru.itis.travelling.data.authregister.remote.api.AuthApi
import ru.itis.travelling.data.authregister.remote.api.RegisterApi
import ru.itis.travelling.data.authregister.remote.model.LoginRequest
import ru.itis.travelling.data.authregister.remote.model.RegistrationRequest
import ru.itis.travelling.data.authregister.remote.model.TokensResponse
import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.profile.locale.database.dao.ParticipantDao
import ru.itis.travelling.domain.authregister.repository.UserRepository
import ru.itis.travelling.domain.authregister.storage.TokenStorage
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val registerApi: RegisterApi,
    private val authApi: AuthApi,
    private val apiHelper: ApiHelper,
    private val tokenStorage: TokenStorage,
    private val participantDao: ParticipantDao
) : UserRepository {

    override suspend fun registerUser(
        phone: String,
        firstName: String,
        lastName: String,
        password: String,
        confirmPassword: String
    ): ResultWrapper<Unit> {
        return apiHelper.safeApiCall {
            val request = RegistrationRequest(
                phoneNumber = phone,
                firstName = firstName,
                lastName = lastName,
                password = password,
                confirmPassword = confirmPassword
            )
            val response = registerApi.register(request)
            apiHelper.handleResponse(response)
            Unit
        }
    }

    override suspend fun login(phone: String, password: String): ResultWrapper<Unit> {
        return apiHelper.safeApiCall {
            val request = LoginRequest(
                phoneNumber = phone,
                password = password
            )

            val response = authApi.login(request)
            val tokens = apiHelper.handleResponse(response)

            tokenStorage.saveTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                expiresIn = tokens.expiresIn
            )
        }
    }

    override suspend fun refreshTokens(refreshToken: String): ResultWrapper<TokensResponse> {
        return apiHelper.safeApiCall {
            val response = authApi.refreshTokens(refreshToken)
            apiHelper.handleResponse(response)
        }
    }

    override suspend fun clearUserData() {
        participantDao.clear()
    }
}