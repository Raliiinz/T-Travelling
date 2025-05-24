package ru.itis.travelling.data.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.util.ErrorHandler
import java.io.IOException
import javax.inject.Inject

class ApiHelper @Inject constructor(
    private val errorHandler: ErrorHandler
) {
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> ResultWrapper.NetworkError
                    is HttpException -> {
                        val code = throwable.code()
                        val errorMessage = errorHandler.getErrorMessage(throwable)
                        ResultWrapper.GenericError(code, errorMessage)
                    }
                    else -> {
                        ResultWrapper.GenericError(null, throwable.message)
                    }
                }
            }
        }
    }
}