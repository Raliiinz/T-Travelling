package ru.itis.travelling.domain.contacts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val id: String,
    val name: String?,
    val phoneNumber: String,
    val isSelected: Boolean = false
) : Parcelable
