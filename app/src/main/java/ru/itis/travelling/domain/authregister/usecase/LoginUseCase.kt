package ru.itis.travelling.domain.authregister.usecase

import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phone: String, password: String): ResultWrapper<Unit> {
        return userRepository.login(phone, password)
    }
}