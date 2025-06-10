package ru.itis.travelling.domain.notifications.model

data class NotificationData(
    val title: String?,
    val message: String?,
//    val type: String,
    val travelId: String?,
    val travelName: String?
)