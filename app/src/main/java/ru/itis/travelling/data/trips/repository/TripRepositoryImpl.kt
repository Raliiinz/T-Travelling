package ru.itis.travelling.data.trips.repository

import retrofit2.HttpException
import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.trips.mapper.TripDetailsMapper
import ru.itis.travelling.data.trips.mapper.TripMapper
import ru.itis.travelling.data.trips.remote.api.TripApi
import ru.itis.travelling.domain.trips.model.Participant
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripApi: TripApi,
    private val apiHelper: ApiHelper,
    private val tripMapper: TripMapper,
    private val tripDetailsMapper: TripDetailsMapper
) : TripRepository {

    // Временное хранилище для демонстрации работы
    private val mockTrips = mutableListOf(
        TripDetails(
            id = "1",
            destination = "Сочи",
            startDate = "12.12.2025",
            endDate = "24.12.2025",
            price = "150000",
            admin = Participant("89274486464"),
            participants = mutableListOf(
                Participant("89274488464"),
                Participant("89274386464"),
                Participant("89374486464")
            )
        ),
        TripDetails(
            id = "2",
            destination = "Москва",
            startDate = "12.12.2026",
            endDate = "24.12.2026",
            price = "130000",
            admin = Participant("89376654566"),
            participants = mutableListOf(
                Participant("89274486464"),
                Participant("89274389964"),
                Participant("89374996464"),
                Participant("89299433464"),
                Participant("89274399464"),
            )
        )
    )

//    override suspend fun getTripsByPhone(phoneNumber: String): List<TripDetails> {
//        return mockTrips.filter { trip ->
//            trip.admin.phone == phoneNumber || trip.participants.any { it.phone == phoneNumber }
//        }
//    }



    override suspend fun getActiveTrips(): ResultWrapper<List<Trip>> {
        return apiHelper.safeApiCall {
            val response = tripApi.getActiveTrips()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            response.body()?.map { tripResponse ->
                tripMapper.mapToDomain(tripResponse)
            } ?: throw IllegalStateException("Response body is null")
        }
    }

    override suspend fun getTripDetails(tripId: String): TripDetails? {
        return mockTrips.find { it.id == tripId }
    }

    override suspend fun leaveTrip(tripId: String, userPhone: String): Boolean {
        val trip = mockTrips.find { it.id == tripId } ?: return false

        if (trip.admin.phone == userPhone) {
            return false
        }

        trip.participants.removeAll { it.phone == userPhone }
        return true
    }

    override suspend fun deleteTrip(tripId: String): Boolean {
        return mockTrips.removeAll { it.id == tripId }
    }

//    override suspend fun createTrip(trip: Trip) {
//        //TODO
////        api.createTrip(trip)
//        val newId = (mockTrips.maxOfOrNull { it.id.toInt() }?.plus(1) ?: 1).toString()
//
//        val newTrip = trip.copy(id = newId)
//
//        mockTrips.add(newTrip)
//    }

    override suspend fun updateTrip(trip: TripDetails) {
        //TODO
        val index = mockTrips.indexOfFirst { it.id == trip.id }

        if (index != -1) {
            mockTrips[index] = trip
        } else {
            println("Trip with id ${trip.id} not found for update")
            throw NoSuchElementException("Trip with id ${trip.id} not found")
        }
    }
//    override suspend fun getTripsByPhone(phoneNumber: String): List<Trip> {
//        TODO("Not yet implemented")
//    }

//    override suspend fun getTripDetails(tripId: String): Trip? {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun leaveTrip(
//        tripId: String,
//        userPhone: String
//    ): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun deleteTrip(tripId: String): Boolean {
//        //TODO("Not yet implemented")
//    }

    override suspend fun createTrip(trip: TripDetails): ResultWrapper<TripDetails> {
        return apiHelper.safeApiCall {
            val request = tripDetailsMapper.mapToRequest(trip)
            val response = tripApi.createTravel(request)

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            response.body()?.let { tripDetailsMapper.mapFromResponse(it) }
                ?: throw IllegalStateException("Empty response body")
        }
    }

//    override suspend fun updateTrip(trip: Trip) {
//        //TODO("Not yet implemented")
//    }
}