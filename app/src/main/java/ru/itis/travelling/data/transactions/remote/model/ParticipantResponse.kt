package ru.itis.travelling.data.transactions.remote.model

import com.google.gson.annotations.SerializedName

data class ParticipantResponse(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("shareAmount") val shareAmount: Double,
    @SerializedName("isRepaid") val isRepaid: Boolean
)