package ru.itis.travelling.presentation.trips.util

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.itis.travelling.R
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ParticipantsPermissionHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activity: FragmentActivity
) {

    private val permissionLauncher by lazy {
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionCallback?.invoke(isGranted)
        }
    }

    private var permissionCallback: ((Boolean) -> Unit)? = null

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
        onPermanentlyDenied: () -> Unit
    ) {
        permissionCallback = { isGranted ->
            when {
                isGranted -> onGranted()
                activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> onDenied()
                else -> onPermanentlyDenied()
            }
        }

        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
//            Toast.makeText(context, R.string.settings_open_error, Toast.LENGTH_SHORT).show()
        }
    }
}