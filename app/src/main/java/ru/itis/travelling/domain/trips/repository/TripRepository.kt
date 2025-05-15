package ru.itis.travelling.domain.trips.repository

import ru.itis.travelling.domain.trips.model.Trip

interface TripRepository {
    suspend fun getTripsByPhone(phoneNumber: String): List<Trip>
    suspend fun getTripDetails(tripId: String): Trip?
    suspend fun leaveTrip(tripId: String, userPhone: String): Boolean
    suspend fun deleteTrip(tripId: String): Boolean
    suspend fun createTrip(trip: Trip)
}
