package ru.itis.travelling.domain.authregister.handler

interface LogoutHandler {
    suspend fun logout()
    fun logoutAsync() // Для вызова из не-suspend контекстов
}