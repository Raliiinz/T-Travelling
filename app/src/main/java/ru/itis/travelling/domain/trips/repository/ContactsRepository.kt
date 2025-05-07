package ru.itis.travelling.domain.trips.repository

import ru.itis.travelling.domain.trips.model.Contact

interface ContactsRepository {
    suspend fun getContacts(): List<Contact>
}