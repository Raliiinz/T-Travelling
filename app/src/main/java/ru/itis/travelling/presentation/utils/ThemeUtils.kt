package ru.itis.travelling.presentation.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemeUtils {
    private const val THEME_PREFERENCE = "theme_preference"
    private const val DARK_THEME = "dark_theme"

    fun applyTheme(context: Context) {
        val isDarkTheme = isDarkTheme(context)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setDarkTheme(context: Context) {
        val sharedPref = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        sharedPref.edit { putBoolean(DARK_THEME, true) }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    fun setLightTheme(context: Context) {
        val sharedPref = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        sharedPref.edit { putBoolean(DARK_THEME, false) }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    fun isDarkTheme(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(DARK_THEME, false)
    }
}
