package ru.itis.travelling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.notifications.model.NotificationData
import ru.itis.travelling.domain.notifications.repository.NotificationsRepository
import ru.itis.travelling.domain.profile.usecase.UpdateDeviceTokenUseCase
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var updateDeviceTokenUseCase: UpdateDeviceTokenUseCase
    @Inject lateinit var notificationsRepository: NotificationsRepository
//    @Inject lateinit var notificationManager: NotificationManagerCompat

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println(token)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                userPreferencesRepository.saveFirebaseToken(token)
                println(userPreferencesRepository.getFirebaseToken())


//                 Если пользователь авторизован - отправляем токен на сервер
                if (userPreferencesRepository.authState.filterNotNull().first().first) {
                    updateDeviceTokenUseCase(token)
                    Log.d("FirebaseService", "Device token updated successfully")
                }
            } catch (e: Exception) {
                Log.e("FirebaseService", "Error handling new token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")
        println("lalalalaalala")
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage)
        }

//        remoteMessage.notification?.let {
//            showSimpleNotification(it.title ?: "Notification", it.body ?: "")
//        }

    }

    private fun handleDataMessage (remoteMessage: RemoteMessage) {
        val message = remoteMessage.toDomainModel()
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.processMessage(message)
        }
    }

//    private fun showSimpleNotification(title: String, message: String) {
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        createDefaultChannel(notificationManager)
//
//        val notification = NotificationCompat.Builder(this, "default_channel_id")
//            .setSmallIcon(R.drawable.ic_travel)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        notificationManager.notify(Random.nextInt(), notification)
//    }
//
//    private fun createDefaultChannel(notificationManager: NotificationManager) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "default_channel_id",
//                "General Notifications",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//    }

    private fun RemoteMessage.toDomainModel(): NotificationData {
        return NotificationData(
            title = data["title"],
            message = data["body"],
            travelId = data["travelId"],
            travelName = data["travelName"]
        )
    }
}