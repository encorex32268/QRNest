package com.lihan.qrnest.feature.scan.presentation

import com.lihan.qrnest.core.domain.model.ScannedBarcode

sealed interface ScanAction {
    data object CloseAppClick : ScanAction
    data object ShowCameraPermissionDialog : ScanAction
    data object DismissCameraPermissionDialog : ScanAction
    data object FlashClick : ScanAction
    data object DismissNoQRCodeFoundDialog : ScanAction
    data class OnBarcodeScanned(val barcode: ScannedBarcode) : ScanAction
    data object OnCameraScanFailed : ScanAction
    data class OnImagePicked(val imageBytes: ByteArray?) : ScanAction {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is OnImagePicked) return false
            return imageBytes.contentEqualsNullable(other.imageBytes)
        }

        override fun hashCode(): Int = imageBytes?.contentHashCode() ?: 0
    }
}

private fun ByteArray?.contentEqualsNullable(other: ByteArray?): Boolean {
    if (this === null && other === null) return true
    if (this === null || other === null) return false
    return this.contentEquals(other)
}
