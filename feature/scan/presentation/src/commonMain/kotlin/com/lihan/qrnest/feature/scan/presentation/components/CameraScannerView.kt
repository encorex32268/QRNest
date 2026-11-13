package com.lihan.qrnest.feature.scan.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lihan.qrnest.core.domain.model.ScannedBarcode

@Composable
expect fun CameraScannerView(
    isFlashOn: Boolean,
    onBarcodeScanned: (ScannedBarcode) -> Unit,
    onScanError: () -> Unit,
    modifier: Modifier = Modifier
)
