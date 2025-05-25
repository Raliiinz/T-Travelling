package ru.itis.travelling.domain.trips.model

data class Participant(
    val phone: String,
    val firstName: String? = null,
    val lastName: String? = null
)

