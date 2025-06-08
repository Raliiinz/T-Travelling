package ru.itis.travelling.domain.notifications.model

data class NotificationConfig(
    val channelId: String,
    val title: String?,
    val message: String?,
    val smallIcon: Int,
    val priority: Int
)