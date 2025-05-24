package ru.itis.travelling.domain.authregister.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.di.qualifies.IoDispatchers
import ru.itis.travelling.domain.authregister.model.User
import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository,
    @IoDispatchers private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(user: User): ResultWrapper<Unit> {
        return withContext(dispatcher) {
            userRepository.registerUser(
                phone = user.phoneNumber,
                firstName = user.firstName,
                lastName = user.lastName,
                password = user.password,
                confirmPassword = user.confirmPassword
            )
        }
    }
}