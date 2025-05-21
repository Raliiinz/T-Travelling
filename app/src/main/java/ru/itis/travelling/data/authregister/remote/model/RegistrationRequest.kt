package ru.itis.travelling.data.authregister.remote.model

import com.google.gson.annotations.SerializedName

data class RegistrationRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("password") val password: String,
    @SerializedName("confirmPassword") val confirmPassword: String
)