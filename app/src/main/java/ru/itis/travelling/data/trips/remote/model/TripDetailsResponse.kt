package ru.itis.travelling.data.trips.remote.model

import com.google.gson.annotations.SerializedName
import ru.itis.travelling.data.profile.remote.model.ParticipantDtoResponse

data class TripDetailsResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("totalBudget") val totalBudget: Double,
    @SerializedName("dateOfBegin") val dateOfBegin: String,
    @SerializedName("dateOfEnd") val dateOfEnd: String,
    @SerializedName("creator") val creator: ParticipantDtoResponse,
    @SerializedName("participants") val participants: List<ParticipantDtoResponse>
)