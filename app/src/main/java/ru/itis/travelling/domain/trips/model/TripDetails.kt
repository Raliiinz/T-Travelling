package ru.itis.travelling.domain.trips.model

import ru.itis.travelling.domain.profile.model.ParticipantDto

data class TripDetails(
    val id: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val price: String,
    val admin: ParticipantDto,
    val participants: MutableList<ParticipantDto>
)
