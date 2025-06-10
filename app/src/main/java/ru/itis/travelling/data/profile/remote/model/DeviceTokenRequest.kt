package ru.itis.travelling.data.profile.remote.model

import com.google.gson.annotations.SerializedName

data class DeviceTokenRequest(
    @SerializedName("deviceToken") val deviceToken: String
)