package com.lihan.qrnest.feature.scan.presentation.components

import android.graphics.Rect
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.domain.parser.BarcodePayloadParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
actual fun CameraScannerView(
    isFlashOn: Boolean,
    onBarcodeScanned: (ScannedBarcode) -> Unit,
    onScanError: () -> Unit,
    modifier: Modifier
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.fillMaxSize().background(Color.DarkGray))
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnScanned by rememberUpdatedState(onBarcodeScanned)
    val latestOnError by rememberUpdatedState(onScanError)

    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(context) {
        try {
            cameraProvider = ProcessCameraProvider.getInstance(context).await()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            latestOnError()
        }
    }

    LaunchedEffect(isFlashOn, camera) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    val imageAnalysis = remember(context) {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    CenterRegionBarcodeAnalyzer(
                        onBarcodeDetected = { scanned -> latestOnScanned(scanned) },
                        onScanFailed = { latestOnError() }
                    )
                )
            }
    }

    val preview = remember { Preview.Builder().build() }

    DisposableEffect(cameraProvider) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    LaunchedEffect(cameraProvider, lifecycleOwner) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val selector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            latestOnError()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                preview.surfaceProvider = surfaceProvider
            }
        }
    )
}

private class CenterRegionBarcodeAnalyzer(
    private val onBarcodeDetected: (ScannedBarcode) -> Unit,
    private val onScanFailed: () -> Unit,
    private val scanDebounceMillis: Long = 1_000L
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )
    private var isScanningAllowed = true
    private val mainScope = CoroutineScope(Dispatchers.Main)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null || !isScanningAllowed) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty() && isScanningAllowed) {
                    val targetRect = Rect(
                        (imageProxy.width * 0.25).roundToInt(),
                        (imageProxy.height * 0.25).roundToInt(),
                        (imageProxy.width * 0.75).roundToInt(),
                        (imageProxy.height * 0.75).roundToInt()
                    )

                    val matched = barcodes.firstOrNull { barcode ->
                        val box = barcode.boundingBox ?: return@firstOrNull false
                        targetRect.contains(box.centerX(), box.centerY())
                    }
                    val rawValue = matched?.rawValue?.takeIf { it.isNotBlank() }
                    if (rawValue != null) {
                        isScanningAllowed = false
                        onBarcodeDetected(BarcodePayloadParser.parse(rawValue))
                        mainScope.launch {
                            delay(scanDebounceMillis)
                            isScanningAllowed = true
                        }
                    }
                }
            }
            .addOnFailureListener {
                onScanFailed()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
