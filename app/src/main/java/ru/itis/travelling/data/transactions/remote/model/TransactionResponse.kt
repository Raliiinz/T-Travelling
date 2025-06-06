package ru.itis.travelling.data.transactions.remote.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("totalCost") val totalCost: Double,
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("category") val category: String
)