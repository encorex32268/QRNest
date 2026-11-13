package com.lihan.qrnest.feature.scan.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberCameraPermissionController(
    onPermissionGranted: () -> Unit
): CameraPermissionController {
    val latestOnGranted by rememberUpdatedState(onPermissionGranted)

    fun evaluateStatus(): CameraPermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> CameraPermissionStatus.GRANTED
            AVAuthorizationStatusDenied,
            AVAuthorizationStatusRestricted -> CameraPermissionStatus.PERMANENTLY_DENIED
            AVAuthorizationStatusNotDetermined -> CameraPermissionStatus.DENIED
            else -> CameraPermissionStatus.DENIED
        }
    }

    var currentStatus by remember { mutableStateOf(evaluateStatus()) }

    DisposableEffect(Unit) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            val previous = currentStatus
            val updated = evaluateStatus()
            currentStatus = updated
            if (previous != CameraPermissionStatus.GRANTED &&
                updated == CameraPermissionStatus.GRANTED
            ) {
                latestOnGranted()
            }
        }
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    return remember {
        object : CameraPermissionController {
            override val status: CameraPermissionStatus
                get() = currentStatus

            override fun requestPermission() {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        currentStatus = evaluateStatus()
                        if (granted) {
                            latestOnGranted()
                        }
                    }
                }
            }

            override fun openSettings() {
                val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
                UIApplication.sharedApplication.openURL(
                    url = url,
                    options = emptyMap<Any?, Any?>(),
                    completionHandler = null
                )
            }
        }
    }
}
