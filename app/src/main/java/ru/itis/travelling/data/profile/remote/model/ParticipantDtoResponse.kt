package ru.itis.travelling.data.profile.remote.model

import com.google.gson.annotations.SerializedName

data class ParticipantDtoResponse(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)