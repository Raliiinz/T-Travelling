package ru.itis.travelling.data.profile.remote.api

import retrofit2.Response
import retrofit2.http.GET
import ru.itis.travelling.data.profile.remote.model.ParticipantDtoResponse

interface ProfileApi {
    @GET("/api/v1/profile")
    suspend fun getProfile(): Response<ParticipantDtoResponse>
}