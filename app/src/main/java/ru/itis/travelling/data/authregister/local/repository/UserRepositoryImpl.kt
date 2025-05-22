package ru.itis.travelling.data.authregister.local.repository

import android.util.Log
import retrofit2.HttpException
import ru.itis.travelling.data.authregister.local.storage.TokenStorage
import ru.itis.travelling.data.authregister.remote.api.AuthApi
import ru.itis.travelling.data.authregister.remote.api.RegisterApi
import ru.itis.travelling.data.authregister.remote.model.LoginRequest
import ru.itis.travelling.data.authregister.remote.model.RefreshTokenRequest
import ru.itis.travelling.data.authregister.remote.model.RegistrationRequest
import ru.itis.travelling.data.authregister.remote.model.TokensResponse
import ru.itis.travelling.domain.authregister.repository.UserRepository
import ru.itis.travelling.domain.exception.NetworkException
import ru.itis.travelling.domain.util.ErrorHandler
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val registerApi: RegisterApi,
    private val authApi: AuthApi,
    private val errorHandler: ErrorHandler,
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
    ) {
        try {
            val request = RegistrationRequest(
                phoneNumber = phone,
                firstName = firstName,
                lastName = lastName,
                password = password,
                confirmPassword = confirmPassword
            )

            val response = registerApi.register(request)

            if (!response.isSuccessful) {
                throw errorHandler.handleHttpException(response.code())
            }
        } catch (ioe: IOException) {
            throw NetworkException("Ошибка сети: ${ioe.message ?: "неизвестная ошибка"}").apply {
                initCause(ioe)
            }
        } catch (httpException: HttpException) {
            throw errorHandler.handleHttpException(httpException.code()).apply {
                initCause(httpException)
            }
        }
    }

    override suspend fun login(phone: String, password: String) {
        try {
            val request = LoginRequest(
                phoneNumber = phone,
                password = password
            )

            val response = authApi.login(request)

            if (!response.isSuccessful) {
                throw errorHandler.handleHttpException(response.code())
            }

            response.body()?.let {
                if (LOG_TOKENS) {
                    Log.d(TAG, """
                    Received tokens:
                    Access: ${tokenStorage.getAccessToken()}... )
                    Refresh: ${tokenStorage.getRefreshToken()}...)
                    """.trimIndent())

//                    logTokenExpiry(tokens.accessToken, "Access Token")
//                    logTokenExpiry(tokens.refreshToken, "Refresh Token")
                }

                tokenStorage.saveTokens(
                    accessToken = it.accessToken,
                    refreshToken = it.refreshToken
                )
                Log.d(TAG, """
                    Received tokens:
                    Access: ${tokenStorage.getAccessToken()}... )
                    Refresh: ${tokenStorage.getRefreshToken()}...)
                    """.trimIndent())
            } ?: throw IllegalStateException("Empty response body")

        } catch (ioe: IOException) {
            throw NetworkException("Network error: ${ioe.message ?: "Unknown error"}").apply {
                initCause(ioe)
            }
        } catch (httpException: HttpException) {
            throw errorHandler.handleHttpException(httpException.code()).apply {
                initCause(httpException)
            }
        }
    }

    override suspend fun refreshTokens(refreshToken: String): TokensResponse {
        try {
            val response = authApi.refreshTokens(RefreshTokenRequest(refreshToken))

            if (!response.isSuccessful) {
                throw errorHandler.handleHttpException(response.code())
            }

            return response.body() ?: throw IllegalStateException("Empty response body")
        } catch (ioe: IOException) {
            throw NetworkException("Network error: ${ioe.message ?: "Unknown error"}").apply {
                initCause(ioe)
            }
        } catch (httpException: HttpException) {
            throw errorHandler.handleHttpException(httpException.code()).apply {
                initCause(httpException)
            }
        }
    }

//    override suspend fun login(phone: String, password: String): Boolean {
//        // TODO: Заменить заглушку на реальную проверку пароля из базы данных
//        // Simulate a login check
//        return password == "111"
//    }
}