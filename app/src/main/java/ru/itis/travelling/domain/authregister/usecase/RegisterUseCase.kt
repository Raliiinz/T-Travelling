package ru.itis.travelling.domain.authregister.usecase

import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phone: String, password: String): Boolean {
        // TODO: Реализовать проверку существования пользователя перед регистрацией
        // if (userRepository.isUserExists(phone)) {
        //     throw IllegalArgumentException("User with this phone number already exists")
        // }

        userRepository.registerUser(phone, password)

        return true
    }
}
