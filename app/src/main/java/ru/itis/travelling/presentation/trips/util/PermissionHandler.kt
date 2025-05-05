package ru.itis.travelling.presentation.trips.util

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PermissionHandler(
    private var onSinglePermissionGranted: (() -> Unit)? = null,
    private var onSinglePermissionDenied: (() -> Unit)? = null
) {
    private var activity: AppCompatActivity? = null

    private var singlePermissionResult: ActivityResultLauncher<String>? = null

    fun initContracts(activity: AppCompatActivity) {
        if (this.activity == null) {
            this.activity = activity
        }

        if (singlePermissionResult == null) {
            singlePermissionResult =
                this.activity?.registerForActivityResult(ActivityResultContracts. RequestPermission()) { isGranted ->
                    if (isGranted) {
                        onSinglePermissionGranted?.invoke()
                    } else {
                        onSinglePermissionDenied?.invoke()
                    }
                }
        }
    }

    fun requestSinglePermission(
        permission: String,
        onGranted: (() -> Unit)? = null,
        onDenied: (() -> Unit)? = null
    ) {
        onSinglePermissionGranted = onGranted
        onSinglePermissionDenied = onDenied
        singlePermissionResult?.launch(permission)
    }
}