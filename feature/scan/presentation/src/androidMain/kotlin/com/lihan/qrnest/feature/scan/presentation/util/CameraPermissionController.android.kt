package com.lihan.qrnest.feature.scan.presentation.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
actual fun rememberCameraPermissionController(
    onPermissionGranted: () -> Unit
): CameraPermissionController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnGranted by rememberUpdatedState(onPermissionGranted)

    var hasRequestedOnce by rememberSaveable { mutableStateOf(false) }

    fun evaluateStatus(): CameraPermissionStatus {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return CameraPermissionStatus.GRANTED

        val activity = context.findActivity()
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } ?: false

        return if (hasRequestedOnce && !shouldShowRationale) {
            CameraPermissionStatus.PERMANENTLY_DENIED
        } else {
            CameraPermissionStatus.DENIED
        }
    }

    var currentStatus by remember { mutableStateOf(evaluateStatus()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRequestedOnce = true
        currentStatus = evaluateStatus()
        if (isGranted) {
            latestOnGranted()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val previous = currentStatus
                val updated = evaluateStatus()
                currentStatus = updated
                if (previous != CameraPermissionStatus.GRANTED &&
                    updated == CameraPermissionStatus.GRANTED
                ) {
                    latestOnGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return remember(context, launcher) {
        object : CameraPermissionController {
            override val status: CameraPermissionStatus
                get() = currentStatus

            override fun requestPermission() {
                hasRequestedOnce = true
                launcher.launch(Manifest.permission.CAMERA)
            }

            override fun openSettings() {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
