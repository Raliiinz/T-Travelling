package ru.itis.travelling.data.authregister.remote

import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.repository.UserRepository
import ru.itis.travelling.domain.authregister.storage.TokenStorage


class ImprovedTokenAuthenticator @Inject constructor(
    private val mutex: Mutex,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Is-Retry") == "true") {
            return null
        }

        return runBlocking {
            if (!mutex.tryLock()) {
                return@runBlocking null
            }

            try {
                if (!tokenStorage.isAccessTokenExpired()) {
                    return@runBlocking null
                }

                val refreshToken = tokenStorage.getRefreshToken() ?: return@runBlocking null

                when (val result = userRepository.refreshTokens(refreshToken)) {
                    is ResultWrapper.Success -> {
                        val newTokens = result.value

                        tokenStorage.saveTokens(
                            accessToken = newTokens.accessToken,
                            refreshToken = newTokens.refreshToken,
                            expiresIn = newTokens.expiresIn ?: 3600
                        )

                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .header("Is-Retry", "true")
                            .removeHeader("Request-ID")
                            .build()
                    }
                    is ResultWrapper.GenericError -> {
                        if (result.code == 401) {
                            tokenStorage.clearTokens()
                        }
                        null
                    }
                    is ResultWrapper.NetworkError -> null
                }
            } catch (e: Exception) {
                null
            } finally {
                mutex.unlock()
            }
        }
    }
}