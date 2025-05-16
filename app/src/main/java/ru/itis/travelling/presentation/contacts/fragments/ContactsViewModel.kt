package ru.itis.travelling.presentation.contacts.fragments

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.itis.travelling.domain.contacts.model.Contact
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor() : ViewModel() {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    private val _selectedContacts = MutableStateFlow<Set<String>>(emptySet())
    val selectedContacts: StateFlow<Set<String>> = _selectedContacts

    fun setContacts(contacts: List<Contact>, initiallySelected: Set<String> = emptySet()) {
        _contacts.value = contacts.map {
            it.copy(isSelected = initiallySelected.contains(it.phoneNumber))
        }
        _selectedContacts.value = initiallySelected.toSet()
    }

    fun toggleContactSelection(contactId: String) {
        val newSelected = _selectedContacts.value.toMutableSet().apply {
            if (contains(contactId)) remove(contactId) else add(contactId)
        }
        _selectedContacts.value = newSelected
        _contacts.value = _contacts.value.map {
            it.copy(isSelected = newSelected.contains(it.phoneNumber))
        }
    }

    fun getSelectedContacts(): List<Contact> {
        return _contacts.value.filter { it.isSelected }
    }
}