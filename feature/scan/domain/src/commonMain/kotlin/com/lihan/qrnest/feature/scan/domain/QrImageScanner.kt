package com.lihan.qrnest.feature.scan.domain

import com.lihan.qrnest.core.domain.model.ScannedBarcode

interface QrImageScanner {
    suspend fun scanImage(imageBytes: ByteArray): ScannedBarcode?
}
