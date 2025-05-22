package ru.itis.travelling.domain.authregister.usecase

import ru.itis.travelling.domain.authregister.model.User
import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Boolean {
        userRepository.registerUser(
            phone = user.phoneNumber,
            firstName = user.firstName,
            lastName = user.lastName,
            password = user.password,
            confirmPassword = user.confirmPassword
        )

        return true
    }
}