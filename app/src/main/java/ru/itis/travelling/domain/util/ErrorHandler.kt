package ru.itis.travelling.domain.util

import ru.itis.travelling.domain.exception.BadRequestException
import ru.itis.travelling.domain.exception.ForbiddenException
import ru.itis.travelling.domain.exception.NotFoundException
import ru.itis.travelling.domain.exception.ServerException
import ru.itis.travelling.domain.exception.UnauthorizedException
import javax.inject.Inject

class ErrorHandler @Inject constructor() {

    companion object {
        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_SERVER_ERROR = 500
    }

    fun handleHttpException(code: Int): Exception {
        return when (code) {
            HTTP_BAD_REQUEST -> BadRequestException()
            HTTP_UNAUTHORIZED -> UnauthorizedException()
            HTTP_FORBIDDEN -> ForbiddenException()
            HTTP_NOT_FOUND -> NotFoundException()
            HTTP_SERVER_ERROR -> ServerException()
            else -> Exception("Ошибка: $code")
        }
    }
}
