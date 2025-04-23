package ru.itis.t_travelling.domain.trips.repository

import ru.itis.t_travelling.domain.trips.model.Trip

interface TripRepository {
    suspend fun getTripsByPhone(phoneNumber: String): List<Trip>
    suspend fun getTripDetails(tripId: String): Trip?
}