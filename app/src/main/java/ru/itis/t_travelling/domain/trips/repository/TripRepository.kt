package ru.itis.t_travelling.domain.trips.repository

import ru.itis.t_travelling.domain.trips.model.Trip

interface TripRepository {
    suspend fun getTripsByUserId(userId: String): List<Trip>
}