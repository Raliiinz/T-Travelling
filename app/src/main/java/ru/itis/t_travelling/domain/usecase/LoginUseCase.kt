package ru.itis.t_travelling.domain.usecase

import ru.itis.t_travelling.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phone: String, password: String): Boolean {
        return userRepository.login(phone, password)
    }
}