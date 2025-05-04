package ru.itis.travelling.data.authregister.local.repository

import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    // TODO: Раскомментировать userDao после подключения базы данных
    // private val userDao: UserDao
) : UserRepository {

    override suspend fun registerUser(phone: String, password: String) {
        // TODO: Реализовать сохранение пользователя в базу данных
        // userDao.insertUser(User(phone = phone, password = password))
    }

    // TODO: Реализовать проверку существования пользователя
    // override suspend fun isUserExists(phone: String): Boolean {
    //     return userDao.getUserByPhone(phone) != null
    // }

    override suspend fun login(phone: String, password: String): Boolean {
        // TODO: Заменить заглушку на реальную проверку пароля из базы данных
        // Simulate a login check
        return password == "111"
    }
}
