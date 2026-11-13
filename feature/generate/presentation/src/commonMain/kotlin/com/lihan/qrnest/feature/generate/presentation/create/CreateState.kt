package com.lihan.qrnest.feature.generate.presentation.create

import androidx.compose.runtime.Immutable
import com.lihan.qrnest.core.domain.model.QRCodeType

@Immutable
data class CreateState(
    val qrCodeType: QRCodeType = QRCodeType.Text,
    val firstInput: String = "",
    val secondInput: String = "",
    val thirdInput: String = "",
    val generateButtonEnabled: Boolean = false,
    val isGenerating: Boolean = false
)
