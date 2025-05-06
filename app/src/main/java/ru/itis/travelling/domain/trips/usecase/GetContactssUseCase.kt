package ru.itis.travelling.domain.trips.usecase

import androidx.fragment.app.FragmentActivity
import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.repository.ContactsRepository
import ru.itis.travelling.domain.trips.repository.ParticipantsRepository
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(
    private val repository: ContactsRepository
) {
    suspend operator fun invoke(): List<Contact> {
        require(repository.hasContactsPermission()) {
            "Contacts permission not granted"
        }
        return repository.getContacts()
    }
}