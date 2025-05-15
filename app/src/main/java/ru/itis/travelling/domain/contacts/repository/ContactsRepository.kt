package ru.itis.travelling.domain.contacts.repository

import ru.itis.travelling.domain.contacts.model.Contact

interface ContactsRepository {
    suspend fun getContacts(): List<Contact>
}