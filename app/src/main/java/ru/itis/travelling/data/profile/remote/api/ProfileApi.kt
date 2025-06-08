package ru.itis.travelling.data.profile.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.itis.travelling.data.profile.remote.model.DeviceTokenRequest
import ru.itis.travelling.data.profile.remote.model.ParticipantDtoResponse

interface ProfileApi {
    @GET("/api/v1/profile")
    suspend fun getProfile(): Response<ParticipantDtoResponse>

    @POST("/api/v1/profile/token")
    suspend fun updateDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>
}