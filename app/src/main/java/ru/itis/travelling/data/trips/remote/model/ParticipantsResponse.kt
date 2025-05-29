package ru.itis.travelling.data.trips.remote.model

import com.google.gson.annotations.SerializedName

data class ParticipantsResponse(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)