package ru.itis.travelling.presentation

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM", "Токен: ${task.result}")
            } else {
                Log.e("FCM", "Ошибка: ${task.exception?.message}")
                // Проверьте google-services.json и интернет-соединение
            }
        }
    }
}
