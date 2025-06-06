package ru.itis.travelling.data.transactions.remote.model

import com.google.gson.annotations.SerializedName
import ru.itis.travelling.data.profile.remote.model.ParticipantDtoResponse

data class TransactionDetailsResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("totalCost") val totalCost: Double,
    @SerializedName("description") val description: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("category") val category: String,
    @SerializedName("participants") val participants: List<ParticipantResponse>,
    @SerializedName("creator") val creator: ParticipantDtoResponse
)