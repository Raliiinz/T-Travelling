package ru.itis.travelling.data.base.repository

import android.content.SharedPreferences
import ru.itis.travelling.domain.base.repository.LocaleRepository
import javax.inject.Inject
import java.util.Locale
import androidx.core.content.edit

class LocaleRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences
) : LocaleRepository {

    override fun getCurrentLanguage(): String {
        return prefs.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: DEFAULT_LANGUAGE
    }

    override fun setLanguage(language: String) {
        prefs.edit { putString(LANGUAGE_KEY, language) }
    }

    companion object {
        const val PREFS_NAME = "locale_prefs"
        const val LANGUAGE_KEY = "selected_language"
        const val DEFAULT_LANGUAGE = "en"
    }
}