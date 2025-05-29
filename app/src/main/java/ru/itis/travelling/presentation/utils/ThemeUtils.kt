package ru.itis.travelling.presentation.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

// ThemeUtils.kt
object ThemeUtils {
    private const val THEME_PREFERENCE = "theme_preference"
    private const val DARK_THEME = "dark_theme"

    fun applyTheme(context: Context) {
        val sharedPref = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean(DARK_THEME, false)

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun toggleTheme(context: Context) {
        val sharedPref = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        val isDarkTheme = !sharedPref.getBoolean(DARK_THEME, false)
        sharedPref.edit().putBoolean(DARK_THEME, isDarkTheme).apply()
        applyTheme(context)
    }

    fun isDarkTheme(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(DARK_THEME, false)
    }
}