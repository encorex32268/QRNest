@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.lihan.qrnest.feature.scan.data

import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.domain.parser.BarcodePayloadParser
import com.lihan.qrnest.feature.scan.domain.QrImageScanner
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNBarcodeSymbologyQR
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler

class VisionQrImageScanner : QrImageScanner {

    override suspend fun scanImage(imageBytes: ByteArray): ScannedBarcode? {
        if (imageBytes.isEmpty()) return null

        return withContext(Dispatchers.Default) {
            try {
                val nsData = imageBytes.usePinned { pinned ->
                    NSData.create(
                        bytes = pinned.addressOf(0),
                        length = imageBytes.size.toULong()
                    )
                }

                ensureActive()

                val request = VNDetectBarcodesRequest().apply {
                    symbologies = listOf(VNBarcodeSymbologyQR)
                }
                val handler = VNImageRequestHandler(
                    data = nsData,
                    options = emptyMap<Any?, Any?>()
                )

                val succeeded = memScoped {
                    val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                    handler.performRequests(listOf(request), errorPtr.ptr)
                }
                if (!succeeded) return@withContext null

                val observations = request.results.orEmpty()
                val rawValue = observations
                    .filterIsInstance<VNBarcodeObservation>()
                    .firstNotNullOfOrNull { observation ->
                        observation.payloadStringValue?.takeIf { it.isNotBlank() }
                    } ?: return@withContext null

                BarcodePayloadParser.parse(rawValue)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                null
            }
        }
    }
}
