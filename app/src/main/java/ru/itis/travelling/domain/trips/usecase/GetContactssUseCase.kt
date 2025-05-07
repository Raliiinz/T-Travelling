package ru.itis.travelling.domain.trips.usecase

import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.domain.trips.repository.ContactsRepository
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val repository: ContactsRepository
) {
    suspend operator fun invoke(): List<Contact> {
       return repository.getContacts()
    }
}