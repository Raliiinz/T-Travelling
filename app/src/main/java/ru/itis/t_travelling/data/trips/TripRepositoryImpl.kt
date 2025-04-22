package ru.itis.t_travelling.data.trips

import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
//    private val tripApi: TripApi,
) : TripRepository {


    override suspend fun getTripsByPhone(phoneNumber: String): List<Trip> {
//        TODO("Not yet implemented")
//        val remoteTrips = tripApi.getTripsByUserId(userId)
//        return remoteTrips
        return emptyList()
    }

}