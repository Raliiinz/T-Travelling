package ru.itis.travelling.data.authregister.remote.model

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(
    @SerializedName("message") val message: String
)