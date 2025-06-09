package ru.itis.travelling.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.domain.notifications.model.NotificationData
import ru.itis.travelling.domain.notifications.repository.NotificationsRepository
import ru.itis.travelling.domain.profile.usecase.UpdateDeviceTokenUseCase
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var updateDeviceTokenUseCase: UpdateDeviceTokenUseCase
    @Inject lateinit var notificationsRepository: NotificationsRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println(token)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                userPreferencesRepository.saveFirebaseToken(token)
                println(userPreferencesRepository.getFirebaseToken())

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
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage)
        }
    }

    private fun handleDataMessage (remoteMessage: RemoteMessage) {
        val message = remoteMessage.toDomainModel()
        CoroutineScope(Dispatchers.IO).launch {
            notificationsRepository.processMessage(message)
        }
    }

    private fun RemoteMessage.toDomainModel(): NotificationData {
        return NotificationData(
            title = data["title"],
            message = data["body"],
            travelId = data["travelId"],
            travelName = data["travelName"]
        )
    }
}