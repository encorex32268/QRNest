package com.lihan.qrnest.feature.preview.presentation

import androidx.compose.runtime.Immutable
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi

@Immutable
data class PreviewState(
    val isScanResult: Boolean = false,
    val qrCodeHistoryUi: QRCodeHistoryUi? = null,
    val titleInput: String = "",
    val isSaving: Boolean = false,
    val isNotFound: Boolean = false
)
