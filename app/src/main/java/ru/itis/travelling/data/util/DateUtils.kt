package ru.itis.travelling.data.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtils {
    private const val API_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    private val apiDateTimeFormatter = DateTimeFormatter.ofPattern(API_DATE_TIME_PATTERN)

    fun formatCurrentTimeForApi(): String {
        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(apiDateTimeFormatter)

        return if (formattedDateTime.length == 19) {
            "$formattedDateTime.${currentDateTime.nano / 1_000_000}"
        } else {
            formattedDateTime
        }
    }

    fun parseApiDateTime(dateTimeString: String): LocalDateTime {
        return LocalDateTime.parse(dateTimeString, apiDateTimeFormatter)
    }
}