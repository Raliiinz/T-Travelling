package ru.itis.travelling.data.trips.repository

import retrofit2.HttpException
import ru.itis.travelling.data.network.ApiHelper
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.data.trips.mapper.TripDetailsMapper
import ru.itis.travelling.data.trips.mapper.TripMapper
import ru.itis.travelling.data.trips.mapper.UpdateTripMapper
import ru.itis.travelling.data.trips.remote.api.TripApi
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.repository.TripRepository
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripApi: TripApi,
    private val apiHelper: ApiHelper,
    private val tripMapper: TripMapper,
    private val tripDetailsMapper: TripDetailsMapper,
    private val updateTripMapper: UpdateTripMapper
) : TripRepository {

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

    override suspend fun getTripDetails(tripId: String): ResultWrapper<TripDetails> {
        return apiHelper.safeApiCall {
            val response = tripApi.getTripDetails(tripId.toLong())
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            response.body()?.let { tripDetailsMapper.mapFromResponse(it) }
                ?: throw IllegalStateException("Response body is null")
        }
    }

    override suspend fun leaveTrip(tripId: String): ResultWrapper<Unit> {
        return apiHelper.safeApiCall {
            val response = tripApi.leaveTrip(tripId.toLong())
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }
    }

    override suspend fun deleteTrip(tripId: String): ResultWrapper<Unit> {
        return apiHelper.safeApiCall {
            val response = tripApi.deleteTravel(tripId.toLong())
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }
    }

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

    override suspend fun updateTrip(trip: TripDetails): ResultWrapper<TripDetails> {
        return apiHelper.safeApiCall {
            val request = updateTripMapper.mapToUpdateRequest(trip)
            val response = tripApi.updateTrip(request)

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            response.body()?.let { tripDetailsMapper.mapFromResponse(it) }
                ?: throw IllegalStateException("Empty response body")
        }
    }
}
