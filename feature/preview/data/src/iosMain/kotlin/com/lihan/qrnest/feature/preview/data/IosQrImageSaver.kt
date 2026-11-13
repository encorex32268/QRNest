@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.lihan.qrnest.feature.preview.data

import androidx.compose.ui.graphics.ImageBitmap
import com.lihan.qrnest.core.domain.util.DataError
import com.lihan.qrnest.core.domain.util.EmptyResult
import com.lihan.qrnest.core.domain.util.Result
import com.lihan.qrnest.feature.preview.domain.QrImageSaver
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGBitmapByteOrder32Little
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToURL
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import kotlin.coroutines.resume
import kotlin.time.Clock

class IosQrImageSaver : QrImageSaver {

    override suspend fun saveQrCode(content: String): EmptyResult<DataError.Local> {
        if (content.isBlank()) {
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        val paddedImage = try {
            val imageBitmap = withContext(Dispatchers.Default) {
                QrPngRenderer.renderImageBitmap(content)
            }
            withContext(Dispatchers.Main) {
                renderPaddedImage(imageBitmap)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        } ?: return Result.Failure(DataError.Local.IMAGE_DECODE_FAILED)

        val currentStatus = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)
        val grantedStatus = if (currentStatus == PHAuthorizationStatusNotDetermined) {
            requestAddOnlyPhotoPermission()
        } else {
            currentStatus
        }

        if (grantedStatus == PHAuthorizationStatusAuthorized ||
            grantedStatus == PHAuthorizationStatusLimited
        ) {
            val savedToPhotos = saveToPhotoLibrary(paddedImage)
            if (savedToPhotos) {
                return Result.Success(Unit)
            }
        }

        return saveToDocumentsFallback(paddedImage)
    }

    private fun renderPaddedImage(imageBitmap: ImageBitmap): UIImage? {
        val srcWidth = imageBitmap.width
        val srcHeight = imageBitmap.height
        if (srcWidth <= 0 || srcHeight <= 0) return null

        val pixels = IntArray(srcWidth * srcHeight)
        imageBitmap.readPixels(pixels)

        val sourceImage = pixels.usePinned { pinned ->
            val colorSpace = CGColorSpaceCreateDeviceRGB() ?: return@usePinned null
            try {
                val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or
                    kCGBitmapByteOrder32Little
                val context = CGBitmapContextCreate(
                    data = pinned.addressOf(0),
                    width = srcWidth.toULong(),
                    height = srcHeight.toULong(),
                    bitsPerComponent = 8uL,
                    bytesPerRow = (srcWidth * 4).toULong(),
                    space = colorSpace,
                    bitmapInfo = bitmapInfo
                ) ?: return@usePinned null
                try {
                    val cgImage = CGBitmapContextCreateImage(context) ?: return@usePinned null
                    try {
                        UIImage.imageWithCGImage(cgImage)
                    } finally {
                        CGImageRelease(cgImage)
                    }
                } finally {
                    CGContextRelease(context)
                }
            } finally {
                CGColorSpaceRelease(colorSpace)
            }
        } ?: return null

        val paddingPx = 60.0
        val totalWidth = srcWidth.toDouble() + paddingPx * 2.0
        val totalHeight = srcHeight.toDouble() + paddingPx * 2.0

        UIGraphicsBeginImageContextWithOptions(CGSizeMake(totalWidth, totalHeight), true, 1.0)
        return try {
            UIColor.whiteColor.setFill()
            UIRectFill(CGRectMake(0.0, 0.0, totalWidth, totalHeight))
            sourceImage.drawInRect(
                CGRectMake(paddingPx, paddingPx, srcWidth.toDouble(), srcHeight.toDouble())
            )
            UIGraphicsGetImageFromCurrentImageContext()
        } finally {
            UIGraphicsEndImageContext()
        }
    }

    private suspend fun requestAddOnlyPhotoPermission(): Long =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
                if (continuation.isActive) {
                    continuation.resume(status)
                }
            }
        }

    private suspend fun saveToPhotoLibrary(image: UIImage): Boolean =
        suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = {
                    PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                },
                completionHandler = { success, _ ->
                    if (continuation.isActive) {
                        continuation.resume(success)
                    }
                }
            )
        }

    private suspend fun saveToDocumentsFallback(image: UIImage): EmptyResult<DataError.Local> {
        val pngData = UIImagePNGRepresentation(image)
            ?: return Result.Failure(DataError.Local.IMAGE_DECODE_FAILED)

        return withContext(Dispatchers.Default) {
            val urls = NSFileManager.defaultManager.URLsForDirectory(
                directory = NSDocumentDirectory,
                inDomains = NSUserDomainMask
            )
            val documentsUrl = urls.firstOrNull() as? NSURL
                ?: return@withContext Result.Failure(DataError.Local.FILE_NOT_FOUND)

            val fileName = "QRNest_${Clock.System.now().toEpochMilliseconds()}.png"
            val targetUrl = documentsUrl.URLByAppendingPathComponent(fileName)
                ?: return@withContext Result.Failure(DataError.Local.FILE_NOT_FOUND)

            val written = pngData.writeToURL(targetUrl, atomically = true)
            if (written) {
                Result.Success(Unit)
            } else {
                Result.Failure(DataError.Local.DISK_FULL)
            }
        }
    }
}
