package ru.itis.travelling.data.authregister.local.repository

import android.util.Log
import retrofit2.HttpException
import ru.itis.travelling.data.authregister.remote.api.AuthApi
import ru.itis.travelling.data.authregister.remote.api.RegisterApi
import ru.itis.travelling.data.authregister.remote.model.LoginRequest
import ru.itis.travelling.data.authregister.remote.model.RefreshTokenRequest
import ru.itis.travelling.data.authregister.remote.model.RegistrationRequest
import ru.itis.travelling.data.authregister.remote.model.TokensResponse
import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.repository.UserRepository
import ru.itis.travelling.domain.authregister.storage.TokenStorage
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val registerApi: RegisterApi,
    private val authApi: AuthApi,
    private val apiHelper: ApiHelper,
    private val tokenStorage: TokenStorage
) : UserRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private const val LOG_TOKENS = true // Включить/выключить логирование токенов
    }

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
            registerApi.register(request).let {
                if (!it.isSuccessful) {
                    throw HttpException(it)
                }
            }
        }
    }

    override suspend fun login(phone: String, password: String): ResultWrapper<Unit> {
        return apiHelper.safeApiCall {
            val request = LoginRequest(
                phoneNumber = phone,
                password = password
            )

            val response = authApi.login(request)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            response.body()?.let { tokens ->
                tokenStorage.saveTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    expiresIn = tokens.expiresIn
                )
            } ?: throw IllegalStateException("Empty response body")
        }
    }

    override suspend fun refreshTokens(refreshToken: String): ResultWrapper<TokensResponse> {
        return apiHelper.safeApiCall {
            val response = authApi.refreshTokens(RefreshTokenRequest(refreshToken))
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            response.body() ?: throw IllegalStateException("Empty response body")
        }
    }

//    override suspend fun login(phone: String, password: String): Boolean {
//        // TODO: Заменить заглушку на реальную проверку пароля из базы данных
//        // Simulate a login check
//        return password == "111"
//    }
}