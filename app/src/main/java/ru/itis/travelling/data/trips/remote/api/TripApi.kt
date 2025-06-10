package ru.itis.travelling.data.trips.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.itis.travelling.data.trips.remote.model.TripDetailsRequest
import ru.itis.travelling.data.trips.remote.model.TripDetailsResponse
import ru.itis.travelling.data.trips.remote.model.TripResponse
import ru.itis.travelling.data.trips.remote.model.UpdateTripRequest

interface TripApi {
    @POST("/api/v1/travels")
    suspend fun createTravel(
        @Body request: TripDetailsRequest
    ): Response<TripDetailsResponse>

    @GET("api/v1/travels/active")
    suspend fun getActiveTrips(): Response<List<TripResponse>>

    @GET("/api/v1/travels/{travelId}")
    suspend fun getTripDetails(
        @Path("travelId") travelId: Long
    ): Response<TripDetailsResponse>

    @DELETE("/api/v1/travels/{travelId}")
    suspend fun deleteTravel(
        @Path("travelId") travelId: Long
    ): Response<Unit>

    @DELETE("/api/v1/travels/leave/{travelId}")
    suspend fun leaveTrip(
        @Path("travelId") travelId: Long
    ): Response<Unit>

    @PUT("/api/v1/travels")
    suspend fun updateTrip(
        @Body request: UpdateTripRequest
    ): Response<TripDetailsResponse>

    @GET("/api/v1/travels/confirm/{travelId}")
    suspend fun confirmParticipation(
        @Path("travelId") travelId: Long
    ): Response<Unit>

    @DELETE("/api/v1/travels/deny/{travelId}")
    suspend fun denyParticipation(
        @Path("travelId") travelId: Long
    ): Response<Unit>
}