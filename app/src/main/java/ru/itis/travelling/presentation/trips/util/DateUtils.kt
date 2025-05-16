package ru.itis.travelling.presentation.trips.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {

    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun formatDateForDisplay(date: LocalDate): String {
        return date.format(displayDateFormatter)
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            formatDateForDisplay(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun calculateNewDates(
        currentDates: Pair<LocalDate, LocalDate>,
        isStartDate: Boolean,
        selectedDate: LocalDate
    ): Pair<LocalDate, LocalDate> {
        return if (isStartDate) {
            selectedDate to if (selectedDate > currentDates.second) selectedDate else currentDates.second
        } else {
            if (selectedDate < currentDates.first) selectedDate to selectedDate else currentDates.first to selectedDate
        }
    }

    fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun LocalDate.toEpochMilli(): Long {
        return atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun String.toLocalDate(): LocalDate {
        return try {
            LocalDate.parse(this)
        } catch (e: DateTimeParseException) {
            try {
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                for (formatter in formatters) {
                    try {
                        return LocalDate.parse(this, formatter)
                    } catch (e: DateTimeParseException) {
                        continue
                    }
                }
                throw IllegalArgumentException("Не удалось распознать дату: $this")
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
    }
}