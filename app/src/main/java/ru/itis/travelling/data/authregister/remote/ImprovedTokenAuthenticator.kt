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
            return null
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

                val newTokens = userRepository.refreshTokens(refreshToken)
                Log.d("Auth", "Tokens refreshed successfully! New access: ${newTokens.accessToken.take(10)}...")

                tokenStorage.saveTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken
                )

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .header("Is-Retry", "true")
                    .build()
            } catch (e: Exception) {
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