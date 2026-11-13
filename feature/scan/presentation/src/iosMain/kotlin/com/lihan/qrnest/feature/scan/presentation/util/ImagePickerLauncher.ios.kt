@file:OptIn(ExperimentalForeignApi::class)

package com.lihan.qrnest.feature.scan.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerLauncher {
    val latestOnPicked by rememberUpdatedState(onImagePicked)

    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                dispatch_async(dispatch_get_main_queue()) {
                    picker.dismissViewControllerAnimated(flag = true, completion = null)
                }

                val firstResult = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
                val itemProvider = firstResult.itemProvider
                if (!itemProvider.hasItemConformingToTypeIdentifier("public.image")) {
                    dispatch_async(dispatch_get_main_queue()) {
                        latestOnPicked(null)
                    }
                    return
                }

                itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                    val bytes = data?.toByteArray()
                    dispatch_async(dispatch_get_main_queue()) {
                        latestOnPicked(bytes)
                    }
                }
            }
        }
    }

    return remember(delegate) {
        object : ImagePickerLauncher {
            override fun launch() {
                val configuration = PHPickerConfiguration().apply {
                    selectionLimit = 1
                    filter = PHPickerFilter.imagesFilter
                }
                val picker = PHPickerViewController(configuration = configuration).apply {
                    this.delegate = delegate
                }
                val presenter = findTopViewController()
                presenter?.presentViewController(
                    viewControllerToPresent = picker,
                    animated = true,
                    completion = null
                )
            }
        }
    }
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)
    val result = ByteArray(size)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return result
}

private fun findTopViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { it.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { it.isKeyWindow() }
        ?: UIApplication.sharedApplication.keyWindow

    var top = keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
