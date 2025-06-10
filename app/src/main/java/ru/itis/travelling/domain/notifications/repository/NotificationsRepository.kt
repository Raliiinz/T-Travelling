package ru.itis.travelling.domain.notifications.repository

import ru.itis.travelling.domain.notifications.model.NotificationData

interface NotificationsRepository {
    suspend fun processMessage(data: NotificationData)
}