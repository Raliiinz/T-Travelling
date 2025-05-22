package ru.itis.travelling.data.authregister.remote.interceptor

import kotlinx.coroutines.sync.Mutex
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UnlockingInterceptor @Inject constructor(
    private val mutex: Mutex
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestId = chain.request().header("Request-ID")
            ?: return chain.proceed(chain.request())

        return try {
            chain.proceed(chain.request())
        } finally {
            if (mutex.holdsLock(requestId)) {
                mutex.unlock(requestId)
            }
        }
    }
}