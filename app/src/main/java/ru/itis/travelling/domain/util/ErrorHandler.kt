package ru.itis.travelling.domain.util

import retrofit2.HttpException
import javax.inject.Inject

class ErrorHandler @Inject constructor() {

    companion object {
        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500
    }

    fun getErrorMessage(throwable: HttpException): String {
        return when (throwable.code()) {
            HTTP_BAD_REQUEST -> "Неверный запрос"
            HTTP_UNAUTHORIZED -> "Требуется авторизация"
            HTTP_FORBIDDEN -> "Доступ запрещен"
            HTTP_NOT_FOUND -> "Ресурс не найден"
            HTTP_SERVER_ERROR -> "Ошибка сервера"
            else -> "Ошибка: ${throwable.code()}"
        }
    }
}
