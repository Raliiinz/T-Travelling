package ru.itis.travelling.presentation.authregister.util

object ValidationUtils {

    private val PHONE_REGEX = Regex("^(?:\\+7\\d{10}|8\\d{10})$")
    private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*\\d).{6,15}$")

    fun isValidPhone(phone: String): Boolean {
        return PHONE_REGEX.matches(phone)
    }

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }
}
