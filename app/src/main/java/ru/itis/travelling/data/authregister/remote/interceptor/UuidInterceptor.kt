package ru.itis.travelling.data.authregister.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject

class UuidInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Request-ID", UUID.randomUUID().toString())
            .build()
        Log.d("UuidInterceptor", "Запрос: ${request.url}, заголовки: ${request.headers}")

        return chain.proceed(request)
    }
}