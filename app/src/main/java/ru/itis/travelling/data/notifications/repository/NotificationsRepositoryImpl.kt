package ru.itis.travelling.data.notifications.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.notifications.model.NotificationChannelConfig
import ru.itis.travelling.domain.notifications.model.NotificationConfig
import ru.itis.travelling.domain.notifications.model.NotificationConstants
import ru.itis.travelling.domain.notifications.model.NotificationData
import ru.itis.travelling.domain.notifications.repository.NotificationsRepository
import ru.itis.travelling.presentation.MainActivity
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : NotificationsRepository {

    private val highPriorityChannelConfig = NotificationChannelConfig(
        id = NotificationConstants.HIGH_PRIORITY_CHANNEL_ID,
        name = NotificationConstants.HIGH_PRIORITY_CHANNEL_NAME,
        importance = NotificationManager.IMPORTANCE_HIGH
    )

    override suspend fun processMessage(data: NotificationData) {
        when (data.title) {
            "Payment reminder" -> showPaymentReminder(data)
            else -> showTravelInvitation(data)
        }
    }

    private fun showPaymentReminder(data: NotificationData) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val channel = NotificationChannel(
            highPriorityChannelConfig.id,
            highPriorityChannelConfig.name,
            highPriorityChannelConfig.importance
        ).apply {
            description = "Important payment reminders"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)


        val notificationConfig = NotificationConfig(
            channelId = highPriorityChannelConfig.id,
            title = data.title,
            message = data.message,
            smallIcon = R.drawable.ic_travel,
            priority = NotificationCompat.PRIORITY_MAX
        )

//        val intent = Intent(context, MainActivity::class.java).apply {
//            putExtra("NAVIGATE_TO", "transactions")
//            putExtra("TRAVEL_ID", data.travelId)
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

//        createNotificationChannel(highPriorityChannelConfig)

        val notification = NotificationCompat.Builder(context, notificationConfig.channelId)
            .setSmallIcon(notificationConfig.smallIcon)
            .setContentTitle(notificationConfig.title)
            .setContentText(notificationConfig.message)
            .setPriority(notificationConfig.priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
//            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationConfig.message))
            .build()

        notificationManager.notify(NotificationConstants.PAYMENT_REMINDER_ID, notification)
    }

    private fun showTravelInvitation(data: NotificationData) {
        CoroutineScope(Dispatchers.IO).launch {
            val phone = getCurrentUserPhone()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            val channel = NotificationChannel(
                highPriorityChannelConfig.id,
                highPriorityChannelConfig.name,
                highPriorityChannelConfig.importance
            ).apply {
                description = "Travel invitations"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)

            val notificationConfig = NotificationConfig(
                channelId = highPriorityChannelConfig.id,
                title = data.title,
                message = data.message ?: "",
                smallIcon = R.drawable.ic_travel,
                priority = NotificationCompat.PRIORITY_HIGH
            )

            // Создаем Intent для открытия деталей поездки
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("NAVIGATE_TO", "trip_details")
                putExtra("TRIP_ID", data.travelId)
                putExtra("PHONE_TEXT", phone)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, notificationConfig.channelId)
                .setSmallIcon(notificationConfig.smallIcon)
                .setContentTitle(notificationConfig.title)
                .setContentText(notificationConfig.message)
                .setPriority(notificationConfig.priority)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationConfig.message))
                .build()

            notificationManager.notify(NotificationConstants.TRAVEL_INVITATION_ID, notification)
        }
    }

    private suspend fun getCurrentUserPhone(): String {
        return userPreferencesRepository.authState.firstOrNull()?.second ?: ""
    }
}