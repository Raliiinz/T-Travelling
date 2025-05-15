package ru.itis.travelling.presentation.trips.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object DateUtils {

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
}