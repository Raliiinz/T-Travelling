package ru.itis.travelling.domain.contacts.usecase

import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.domain.contacts.repository.ContactsRepository
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val repository: ContactsRepository
) {
    suspend operator fun invoke(): List<Contact> {
       return repository.getContacts()
    }
}