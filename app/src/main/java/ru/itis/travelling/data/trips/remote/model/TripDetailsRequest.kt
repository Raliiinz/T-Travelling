package ru.itis.travelling.data.trips.remote.model

import com.google.gson.annotations.SerializedName

data class TripDetailsRequest(
    @SerializedName("name") val name: String,
    @SerializedName("totalBudget") val totalBudget: Double,
    @SerializedName("dateOfBegin") val dateOfBegin: String,
    @SerializedName("dateOfEnd") val dateOfEnd: String,
    @SerializedName("participantPhones") val participantPhones: List<String>
)
