package com.lihan.qrnest.feature.history.presentation

import androidx.compose.runtime.Stable
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi

@Stable
data class ScanHistoryState(
    val scannedItems: List<QRCodeHistoryUi> = emptyList(),
    val generatedItems: List<QRCodeHistoryUi> = emptyList(),
    val isShowEditorBottomSheet: Boolean = false,
    val selectedId: Long? = null
)
