package ru.itis.travelling.data.authregister.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import ru.itis.travelling.data.authregister.remote.model.LoginRequest
import ru.itis.travelling.data.authregister.remote.model.LoginResponse
import ru.itis.travelling.data.authregister.remote.model.RefreshTokenRequest
import ru.itis.travelling.data.authregister.remote.model.TokensResponse
import ru.itis.travelling.data.profile.remote.model.DeviceTokenRequest

interface AuthApi {
    @POST("/api/v1/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @FormUrlEncoded
    @POST("/api/v1/refresh")
    suspend fun refreshTokens(
        @Field("refreshToken") refreshToken: String
    ): Response<TokensResponse>
}