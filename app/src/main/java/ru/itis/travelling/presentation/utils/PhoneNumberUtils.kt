package ru.itis.travelling.presentation.utils

object PhoneNumberUtils {
    private const val RUSSIAN_PREFIX = "+7"

    fun formatPhoneNumber(input: String): String {
        val digits = input.filter { it.isDigit() }

        val normalized = when {
            digits.startsWith("8") -> "7" + digits.drop(1)
            digits.startsWith("7") -> digits
            digits.startsWith("9") -> "7$digits"
            else -> digits
        }

        val number = normalized.drop(1)

        val builder = StringBuilder(RUSSIAN_PREFIX)

        when (number.length) {
            in 1..3 -> builder.append(" (${number}")
            in 4..6 -> builder.append(" (${number.substring(0,3)}) ${number.substring(3)}")
            in 7..8 -> builder.append(" (${number.substring(0,3)}) ${number.substring(3,6)}-${number.substring(6)}")
            in 9..10 -> builder.append(" (${number.substring(0,3)}) ${number.substring(3,6)}-${number.substring(6,8)}-${number.substring(8)}")
            else -> {
                if (number.isNotEmpty()) {
                    builder.append(" (${number.take(3)}) ")
                    builder.append(number.drop(3).chunked(2).joinToString("-").take(10))
                }
            }
        }

        return builder.toString()
    }

    fun normalizePhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }

        return when {
            digits.startsWith("8") -> digits
            digits.startsWith("7") -> "8" + digits.drop(1)
            digits.startsWith("9") -> "8$digits"
            else -> digits
        }
    }
}