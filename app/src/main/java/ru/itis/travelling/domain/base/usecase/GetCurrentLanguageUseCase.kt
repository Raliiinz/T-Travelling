package ru.itis.travelling.domain.base.usecase

import ru.itis.travelling.domain.base.repository.LocaleRepository
import javax.inject.Inject

class GetCurrentLanguageUseCase @Inject constructor(
    private val repository: LocaleRepository
) {
    operator fun invoke(): String = repository.getCurrentLanguage()
}