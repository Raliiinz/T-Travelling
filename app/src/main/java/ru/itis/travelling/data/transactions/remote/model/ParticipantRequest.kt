package ru.itis.travelling.data.transactions.remote.model

import com.google.gson.annotations.SerializedName

data class ParticipantRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("shareAmount") val shareAmount: String
)