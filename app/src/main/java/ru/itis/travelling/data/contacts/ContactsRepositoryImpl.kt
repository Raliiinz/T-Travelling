package ru.itis.travelling.data.contacts

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.domain.contacts.repository.ContactsRepository
import javax.inject.Inject

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
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex)
                    val phoneNumber = cursor.getString(phoneIndex)?.replace("\\D".toRegex(), "")

                    if (!phoneNumber.isNullOrBlank()) {
                        contacts.add(Contact(name, phoneNumber))
                    }
                }
            }
            contacts.distinctBy { it.phoneNumber }
        }
    }
}
