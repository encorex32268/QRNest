package com.lihan.qrnest.feature.scan.presentation.util

import androidx.compose.runtime.Composable

enum class CameraPermissionStatus {
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

interface CameraPermissionController {
    val status: CameraPermissionStatus
    fun requestPermission()
    fun openSettings()
}

@Composable
expect fun rememberCameraPermissionController(
    onPermissionGranted: () -> Unit
): CameraPermissionController
