package ru.itis.travelling.data.trips.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.itis.travelling.data.trips.remote.model.TripDetailsRequest
import ru.itis.travelling.data.trips.remote.model.TripDetailsResponse
import ru.itis.travelling.data.trips.remote.model.TripResponse

interface TripApi {
    @POST("/api/v1/travels/create")
    suspend fun createTravel(
        @Body request: TripDetailsRequest
    ): Response<TripDetailsResponse>

    @GET("api/v1/travels/active")
    suspend fun getActiveTravels(): Response<List<TripResponse>>
}