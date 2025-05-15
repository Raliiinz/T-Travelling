package ru.itis.travelling.presentation.authregister.util

import ru.itis.travelling.presentation.utils.PhoneNumberUtils

object ValidationUtils {

    private val PASSWORD_REGEX = Regex("^(?=.*[A-Z])(?=.*\\d).{6,15}$")

    fun isValidPhone(phone: String): Boolean {
        val normalized = PhoneNumberUtils.normalizePhoneNumber(phone)
        return normalized.length == 11 && normalized.startsWith("89")
    }

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }
}
