package ru.itis.travelling.data.authregister.remote.model

import com.google.gson.annotations.SerializedName

data class TokensResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long? = null
)