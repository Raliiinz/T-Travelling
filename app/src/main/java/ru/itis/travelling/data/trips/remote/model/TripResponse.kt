package ru.itis.travelling.data.trips.remote.model

import com.google.gson.annotations.SerializedName

data class TripResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("totalBudget") val totalBudget: Double,
    @SerializedName("dateOfBegin") val dateOfBegin: String,
    @SerializedName("dateOfEnd") val dateOfEnd: String
)