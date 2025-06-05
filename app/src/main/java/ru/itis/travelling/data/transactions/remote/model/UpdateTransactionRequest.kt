package ru.itis.travelling.data.transactions.remote.model

import com.google.gson.annotations.SerializedName

data class UpdateTransactionRequest(
    @SerializedName("category") val category: String,
    @SerializedName("totalCost") val totalCost: Double,
    @SerializedName("description") val description: String,
    @SerializedName("participant") val participant: List<ParticipantRequest>
)