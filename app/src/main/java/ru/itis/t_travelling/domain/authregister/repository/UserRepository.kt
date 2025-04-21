package ru.itis.t_travelling.domain.authregister.repository

interface UserRepository {
    suspend fun registerUser(phone: String, password: String)

    // TODO: Реализовать метод для проверки существования пользователя
    // suspend fun isUserExists(phone: String): Boolean

    suspend fun login(phone: String, password: String): Boolean
}