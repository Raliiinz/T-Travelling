package ru.itis.travelling.data.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.repository.ContactsRepository
import ru.itis.travelling.domain.trips.repository.ParticipantsRepository
import javax.inject.Inject

// features/addtrip/data/repository/ContactsRepositoryImpl.kt
class ContactsRepositoryImpl @Inject constructor(
    private val context: Context
) : ContactsRepository {

    override suspend fun getContacts(): List<Contact> {
        return withContext(Dispatchers.IO) {
            val contacts = mutableListOf<Contact>()
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val selection = "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} = 1"
            val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

            context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val phoneNumber = cursor.getString(phoneIndex)?.replace("\\D".toRegex(), "")

                    if (!phoneNumber.isNullOrBlank()) {
                        contacts.add(Contact(id, name, phoneNumber))
                    }
                }
            }
            contacts.distinctBy { it.phoneNumber } // Remove duplicates
        }
    }

    override fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            CONTACTS_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        const val CONTACTS_PERMISSION_REQUEST_CODE = 1001
    }
}