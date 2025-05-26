package ru.itis.travelling.domain.trips.repository

import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.model.TripDetails

interface TripRepository {
    suspend fun getActiveTrips(): ResultWrapper<List<Trip>>
    suspend fun getTripDetails(tripId: String): ResultWrapper<TripDetails>
    suspend fun leaveTrip(tripId: String): ResultWrapper<Unit>
    suspend fun deleteTrip(tripId: String): ResultWrapper<Unit>
    suspend fun createTrip(trip: TripDetails): ResultWrapper<TripDetails>
    suspend fun updateTrip(trip: TripDetails): ResultWrapper<TripDetails>
}
