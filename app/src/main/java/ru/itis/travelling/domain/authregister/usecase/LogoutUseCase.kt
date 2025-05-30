package ru.itis.travelling.domain.authregister.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): ResultWrapper<Unit> {
        return withContext(dispatcher) {
            try {
                userPreferencesRepository.clearAuthData()
                userRepository.clearUserData()
                ResultWrapper.Success(Unit)
            } catch (e: Exception) {
                ResultWrapper.GenericError(
                    code = null,
                    error = e.message ?: "Logout failed"
                )
            }
        }
    }
}