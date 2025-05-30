package ru.itis.travelling.domain.base.usecase

import ru.itis.travelling.domain.base.repository.LocaleRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val repository: LocaleRepository
) {
    operator fun invoke(language: String) {
        repository.setLanguage(language)
    }
}