@file:OptIn(ExperimentalForeignApi::class)

package com.lihan.qrnest.feature.scan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.domain.parser.BarcodePayloadParser
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isTorchModeSupported
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import kotlin.time.Clock

@Composable
actual fun CameraScannerView(
    isFlashOn: Boolean,
    onBarcodeScanned: (ScannedBarcode) -> Unit,
    onScanError: () -> Unit,
    modifier: Modifier
) {
    val latestOnScanned by rememberUpdatedState(onBarcodeScanned)
    val latestOnError by rememberUpdatedState(onScanError)

    val captureDevice = remember {
        AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
    }

    if (captureDevice == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black))
        return
    }

    val sessionQueue = remember {
        dispatch_queue_create("com.lihan.qrnest.camera.session", null)
    }

    val captureSession = remember {
        AVCaptureSession().apply {
            sessionPreset = AVCaptureSessionPresetHigh
        }
    }

    val metadataDelegate = remember {
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            private var lastScannedTimestampMs = 0L

            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection
            ) {
                val now = Clock.System.now().toEpochMilliseconds()
                if (now - lastScannedTimestampMs < 1_000L) return

                val codeObject = didOutputMetadataObjects
                    .filterIsInstance<AVMetadataMachineReadableCodeObject>()
                    .firstOrNull { !it.stringValue.isNullOrBlank() }
                    ?: return

                val rawValue = codeObject.stringValue ?: return
                lastScannedTimestampMs = now

                dispatch_async(dispatch_get_main_queue()) {
                    latestOnScanned(BarcodePayloadParser.parse(rawValue))
                }
            }
        }
    }

    LaunchedEffect(captureDevice) {
        dispatch_async(sessionQueue) {
            val input = try {
                AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, null)
            } catch (_: Exception) {
                null
            }
            val metadataOutput = AVCaptureMetadataOutput()

            captureSession.beginConfiguration()
            var configured = false
            if (input != null &&
                captureSession.canAddInput(input) &&
                captureSession.canAddOutput(metadataOutput)
            ) {
                captureSession.addInput(input)
                captureSession.addOutput(metadataOutput)
                metadataOutput.setMetadataObjectsDelegate(
                    metadataDelegate,
                    queue = dispatch_get_main_queue()
                )
                metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                configured = true
            }
            captureSession.commitConfiguration()

            if (configured && !captureSession.running) {
                captureSession.startRunning()
            } else if (!configured) {
                dispatch_async(dispatch_get_main_queue()) {
                    latestOnError()
                }
            }
        }
    }

    LaunchedEffect(isFlashOn, captureDevice) {
        if (!captureDevice.hasTorch) return@LaunchedEffect
        val targetMode = if (isFlashOn) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        if (!captureDevice.isTorchModeSupported(targetMode)) return@LaunchedEffect

        if (captureDevice.lockForConfiguration(null)) {
            try {
                captureDevice.torchMode = targetMode
            } finally {
                captureDevice.unlockForConfiguration()
            }
        }
    }

    DisposableEffect(captureSession) {
        onDispose {
            dispatch_async(sessionQueue) {
                if (captureSession.running) {
                    captureSession.stopRunning()
                }
            }
        }
    }

    UIKitView(
        modifier = modifier.fillMaxSize(),
        factory = {
            CameraPreviewUIView(captureSession = captureSession)
        }
    )
}

private class CameraPreviewUIView(
    captureSession: AVCaptureSession
) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {

    private val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        backgroundColor = UIColor.blackColor
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        val currentBounds: CValue<CGRect> = bounds
        previewLayer.frame = currentBounds
    }
}
