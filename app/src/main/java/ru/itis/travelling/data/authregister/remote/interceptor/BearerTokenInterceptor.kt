package ru.itis.travelling.data.authregister.remote.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import ru.itis.travelling.domain.authregister.storage.TokenStorage

class BearerTokenInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.url.encodedPath.contains("refresh")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking {
            tokenStorage.getAccessToken()
        }

        return if (!accessToken.isNullOrBlank()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}