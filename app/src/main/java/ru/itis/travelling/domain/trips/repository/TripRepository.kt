package ru.itis.travelling.domain.trips.repository

import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.trips.model.TripDetails

interface TripRepository {
    suspend fun getTripsByPhone(phoneNumber: String): List<TripDetails>
    suspend fun getTripDetails(tripId: String): TripDetails?
    suspend fun leaveTrip(tripId: String, userPhone: String): Boolean
    suspend fun deleteTrip(tripId: String): Boolean
    suspend fun createTrip(trip: TripDetails): ResultWrapper<TripDetails>
    suspend fun updateTrip(trip: TripDetails)
}
