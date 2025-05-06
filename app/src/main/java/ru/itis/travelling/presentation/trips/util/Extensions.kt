package ru.itis.travelling.presentation.trips.util

import android.os.Build
import android.os.Bundle
import android.os.Parcelable

inline fun <reified T : Parcelable> Bundle.getParcelableArrayListSafe(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableArrayList(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableArrayList(key)
    }
}
