package ru.itis.travelling.data.authregister.remote

import android.util.Log
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

private const val TAG = "ImprovedTokenAuthenticator"

class ImprovedTokenAuthenticator @Inject constructor(
    private val mutex: Mutex,
    private val tokenStorage: TokenStorage,
    private val userRepository: UserRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(TAG, "Получен ответ: ${response.code}, проверяю необходимость обновления токена")

        if (response.request.header("Is-Retry") == "true") {
            Log.w(TAG, "Запрос уже был повторен. Отказываемся обновлять токен.")
            return null
        }

        return runBlocking {
            Log.d(TAG, "Попытка заблокировать мьютекс...")
            if (!mutex.tryLock()) {
                Log.w(TAG, "Не удалось получить мьютекс. Кто-то уже работает с токенами.")
                return@runBlocking null
            }

            try {
                if (!tokenStorage.hasRefreshToken()) {
                    Log.e(TAG, "No refresh token available. Clearing all tokens.")
                    tokenStorage.clearTokens()
                    return@runBlocking null
                }

                Log.d(TAG, "Проверяем, истёк ли access-токен...")
                if (!tokenStorage.isAccessTokenExpired()) {
                    val currentToken = tokenStorage.getAccessToken()
                    if (!currentToken.isNullOrBlank()) {
                        Log.w(TAG, "Токен ещё актуален, но сервер вернул 401. Повторим запрос с текущим токеном.")
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .header("Is-Retry", "true")
                            .removeHeader("Request-ID")
                            .build()
                    } else {
                        Log.e(TAG, "Access-токен отсутствует.")
                        return@runBlocking null
                    }
                }
                Log.i(TAG, "Access-токен истёк. Нужно обновить.")

                val refreshToken = tokenStorage.getRefreshToken()
                if (refreshToken.isNullOrBlank()) {
                    Log.e(TAG, "Refresh-токен отсутствует. Невозможно обновить токены.")
                    return@runBlocking null
                }

                Log.d(TAG, "Запрашиваем новые токены через refreshTokensUseCase...")

                val refreshToke = tokenStorage.getRefreshToken()
                Log.d(TAG, "Текущий refreshToken: $refreshToke")

                when (val result = userRepository.refreshTokens(refreshToken)) {
                    is ResultWrapper.Success -> {
                        val newTokens = result.value
                        Log.i(TAG, "Успешно получены новые токены.")

                        tokenStorage.saveTokens(
                            accessToken = newTokens.accessToken,
                            refreshToken = newTokens.refreshToken,
                            expiresIn = newTokens.expiresIn ?: 3600
                        )

                        Log.d(TAG, "Создаём новый запрос с новым access-токеном...")
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .header("Is-Retry", "true")
                            .removeHeader("Request-ID")
                            .build()
                    }
                    is ResultWrapper.GenericError -> {
                        Log.e(TAG, "Ошибка при обновлении токена: ${result.error}, код: ${result.code}")
                        if (result.code == 401) {
                            Log.w(TAG, "Пользователь разлогинен. Очищаем токены.")
                            tokenStorage.clearTokens()
                        }
                        null
                    }
                    is ResultWrapper.NetworkError -> {
                        Log.e(TAG, "Сетевая ошибка при обновлении токена.")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при обновлении токена", e)
                null
            } finally {
                Log.d(TAG, "Разблокируем мьютекс.")
                mutex.unlock()
            }
        }
    }
}
