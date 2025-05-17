package ru.itis.travelling.presentation.trips.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY)

    fun formatPriceWithThousands(price: String?): String {
        return try {
            val number = price?.toIntOrNull()
            number?.let { numberFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
