package com.lihan.qrnest.feature.scan.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.domain.parser.BarcodePayloadParser
import com.lihan.qrnest.feature.scan.domain.QrImageScanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MlKitQrImageScanner : QrImageScanner {

    override suspend fun scanImage(imageBytes: ByteArray): ScannedBarcode? {
        if (imageBytes.isEmpty()) return null

        return withContext(Dispatchers.Default) {
            var bitmap: Bitmap? = null
            try {
                bitmap = decodeDownsampledBitmap(imageBytes, maxDimension = 2000)
                    ?: return@withContext null

                ensureActive()
                val scanner = BarcodeScanning.getClient()
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val barcodes = scanner.process(inputImage).await()
                val rawValue = barcodes.firstOrNull()?.rawValue?.takeIf { it.isNotBlank() }
                    ?: return@withContext null

                BarcodePayloadParser.parse(rawValue)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                null
            } finally {
                bitmap?.recycle()
            }
        }
    }

    private fun decodeDownsampledBitmap(bytes: ByteArray, maxDimension: Int): Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

        var sampleSize = 1
        while (boundsOptions.outWidth / sampleSize > maxDimension ||
            boundsOptions.outHeight / sampleSize > maxDimension
        ) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    }
}
