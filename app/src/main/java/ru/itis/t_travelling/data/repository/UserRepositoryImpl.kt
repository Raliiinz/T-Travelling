package ru.itis.t_travelling.data.repository

import ru.itis.t_travelling.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
//    private val userDao: UserDao
) : UserRepository {

    override suspend fun registerUser(phone: String, password: String) {
        // Логика сохранения пользователя в базу данных
//        userDao.insertUser(User(phone = phone, password = password))
    }

//    override suspend fun isUserExists(phone: String): Boolean {
//        return userDao.getUserByPhone(phone) != null
//    }

    override suspend fun login(phone: String, password: String): Boolean {
        // Simulate a login check
        return password == "111"
    }
}