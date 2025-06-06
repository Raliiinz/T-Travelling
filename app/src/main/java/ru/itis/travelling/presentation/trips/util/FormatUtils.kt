package ru.itis.travelling.presentation.trips.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY)

    fun formatPriceWithThousands(price: String?): String {
        return try {
            val number = price?.toDoubleOrNull()
            number?.let { numberFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun formatPriceWithoutSeparators(price: String?): String {
        return try {
            // Удаляем все разделители тысяч и заменяем запятую на точку для десятичных
            price?.replace(".", "")?.replace(",", ".") ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
