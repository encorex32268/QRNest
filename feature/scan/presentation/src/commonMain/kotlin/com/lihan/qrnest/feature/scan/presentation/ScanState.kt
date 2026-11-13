package com.lihan.qrnest.feature.scan.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class ScanState(
    val isShowCameraPermissionDialog: Boolean = false,
    val isShowNoQRCodesFound: Boolean = false,
    val isLoading: Boolean = false,
    val isOpeningFlashlight: Boolean = false
)
