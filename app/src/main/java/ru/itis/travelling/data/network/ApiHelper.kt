package ru.itis.travelling.data.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.util.ErrorHandler
import java.io.IOException
import javax.inject.Inject
import android.util.Log

private const val TAG = "ApiHelper"

class ApiHelper @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            Log.d(TAG, "Начинаем выполнение API-запроса...")
            try {
                val result = apiCall.invoke()
                Log.d(TAG, "API-запрос успешно выполнен.")
                ResultWrapper.Success(result)
            } catch (throwable: Throwable) {
                Log.e(TAG, "Ошибка при выполнении API-запроса", throwable)
                when (throwable) {
                    is IOException -> {
                        Log.w(TAG, "Сетевая ошибка: ${throwable.message}")
                        ResultWrapper.NetworkError
                    }
                    is HttpException -> {
                        val code = throwable.code()
                        val errorMessage = errorHandler.getErrorMessage(throwable)
                        Log.e(TAG, "HTTP ошибка $code: $errorMessage")
                        ResultWrapper.GenericError(code, errorMessage)
                    }
                    else -> {
                        Log.e(TAG, "Неизвестная ошибка: ${throwable.message}", throwable)
                        ResultWrapper.GenericError(null, throwable.message)
                    }
                }
            }
        }
    }
}