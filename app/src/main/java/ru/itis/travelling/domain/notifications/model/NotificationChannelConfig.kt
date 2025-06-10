package ru.itis.travelling.domain.notifications.model

data class NotificationChannelConfig(
    val id: String,
    val name: String,
    val importance: Int
)