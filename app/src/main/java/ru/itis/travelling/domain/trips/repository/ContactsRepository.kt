package ru.itis.travelling.domain.trips.repository

import androidx.fragment.app.FragmentActivity
import ru.itis.travelling.domain.trips.model.Contact

interface ContactsRepository {
    suspend fun getContacts(): List<Contact>
    fun hasContactsPermission(): Boolean
    fun requestPermission(activity: FragmentActivity, onResult: (Boolean) -> Unit)
}