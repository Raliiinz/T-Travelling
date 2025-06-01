package ru.itis.travelling.domain.base.repository

interface LocaleRepository {
    fun getCurrentLanguage(): String
    fun setLanguage(language: String)
}