package ru.itis.travelling.data.authregister.remote

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import ru.itis.travelling.data.authregister.local.storage.TokenStorage
import ru.itis.travelling.domain.authregister.repository.UserRepository


class ImprovedTokenAuthenticator @Inject constructor(
    private val mutex: Mutex,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("Auth", "Attempting token refresh...")
        if (response.request.header("Is-Retry") == "true") {
            return null // Prevent infinite loops
        }

        return runBlocking {
            if (!mutex.tryLock()) {
                return@runBlocking null
            }

            try {
                val refreshToken = tokenStorage.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    return@runBlocking null
                }

                // Refresh tokens
                val newTokens = userRepository.refreshTokens(refreshToken)
                Log.d("Auth", "Tokens refreshed successfully! New access: ${newTokens.accessToken.take(10)}...")

                // Save new tokens
                tokenStorage.saveTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken
                )

                // Get new access token
                //[p[p[p
//                val newAccessToken = tokenStorage.getAccessToken() ?: return@runBlocking null

                // Retry the original request with new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .header("Is-Retry", "true")
                    .build()
            } catch (e: Exception) {
                Log.e("Auth", "Refresh failed: ${e.javaClass.simpleName} - ${e.message}")
                if (e is HttpException && e.code() == 401) {
                    tokenStorage.clearTokens()
                }
                null
            } finally {
                mutex.unlock()
            }
        }
    }
}

//
//class TokenAuthenticator @Inject constructor(
//    private val mutex: Mutex,
//    @LogoutStatus private val isLogoutStarted: Boolean,
//    private val logoutHandler: LogoutHandler,
//    private val tokenStorage: TokenStorage,
//    private val authApiService: AuthApiService // Для обновления токена
//) : Authenticator {
//
//    private companion object {
//        const val MAX_RETRIES = 3
//    }
//
//    override fun authenticate(route: Route?, response: Response): Request? = runBlocking {
//        if (isLogoutStarted) return@runBlocking null
//        val requestId = response.request.header("Request-ID") ?: return@runBlocking null
//        val retryCount = response.request.header("Retry-Count")?.toIntOrNull() ?: 0
//
//        if (retryCount >= MAX_RETRIES) {
//            logoutHandler.logout()
//            return@runBlocking null
//        }
//
//        mutex.withLock(requestId) {
//            refreshToken()?.let { newToken ->
//                response.request.newBuilder()
//                    .header("Authorization", "Bearer $newToken")
//                    .header("Retry-Count", (retryCount + 1).toString())
//                    .build()
//            }
//        }
//    }
//
//    private suspend fun refreshToken(): String? {
//        val refreshToken = tokenStorage.getRefreshToken() ?: return null
//        return try {
//            val response = authApiService.refreshToken(refreshToken)
//            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
//            response.accessToken
//        } catch (e: Exception) {
//            null
//        }
//    }
//}