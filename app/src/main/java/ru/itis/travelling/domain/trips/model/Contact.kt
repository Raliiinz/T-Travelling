package ru.itis.travelling.domain.trips.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val id: String,
    val name: String?,
    val phoneNumber: String,
    var isSelected: Boolean = false
) : Parcelable