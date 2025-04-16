package ru.itis.t_travelling.domain.usecase

import ru.itis.t_travelling.domain.repository.UserPreferencesRepository
import ru.itis.t_travelling.domain.repository.UserRepository
import ru.itis.t_travelling.util.ValidationUtils
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phone: String, password: String): Boolean {
//        if (userRepository.isUserExists(phone)) {
//            throw IllegalArgumentException("User with this phone number already exists")
//        }

        userRepository.registerUser(phone, password)

        return true
    }
}