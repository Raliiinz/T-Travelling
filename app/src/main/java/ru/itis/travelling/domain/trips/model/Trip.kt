package ru.itis.travelling.domain.trips.model

data class Trip (
    val id: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val price: String,
)
