package ru.itis.t_travelling.presentation.trips.util

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {

    fun formatPriceWithThousands(price: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.GERMANY)
        return formatter.format(price)
    }
}