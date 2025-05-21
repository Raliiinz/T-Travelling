package ru.itis.travelling.data.authregister.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.itis.travelling.data.authregister.remote.model.RegistrationRequest
import ru.itis.travelling.data.authregister.remote.model.RegistrationResponse

interface RegisterApi {
    @POST("/api/v1/registration")
    suspend fun register(@Body request: RegistrationRequest): Response<RegistrationResponse>
}