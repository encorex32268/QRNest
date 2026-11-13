package com.lihan.qrnest.core.presentation.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.contact
import com.lihan.qrnest.core.designsystem.resources.geo_location
import com.lihan.qrnest.core.designsystem.resources.link
import com.lihan.qrnest.core.designsystem.resources.phone_number
import com.lihan.qrnest.core.designsystem.resources.text
import com.lihan.qrnest.core.designsystem.resources.wifi
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.QRCodeType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun QRCodeType.toStringResource(): StringResource {
    return when (this) {
        QRCodeType.Text -> Res.string.text
        QRCodeType.Link -> Res.string.link
        QRCodeType.Contact -> Res.string.contact
        QRCodeType.PhoneNumber -> Res.string.phone_number
        QRCodeType.Geolocation -> Res.string.geo_location
        QRCodeType.WiFi -> Res.string.wifi
    }
}

@Composable
fun QRCodeType.asString(): String {
    return stringResource(toStringResource())
}

@Composable
fun QRCodeType.toImageVector(): ImageVector {
    return when (this) {
        QRCodeType.Text -> QRNestIcons.Text
        QRCodeType.Link -> QRNestIcons.Link
        QRCodeType.Contact -> QRNestIcons.User
        QRCodeType.PhoneNumber -> QRNestIcons.Phone
        QRCodeType.Geolocation -> QRNestIcons.MarkerPin
        QRCodeType.WiFi -> QRNestIcons.Wifi
    }
}

@Composable
fun QRCodeType.toIconTint(): Color {
    val colors = MaterialTheme.colorScheme.appColors
    return when (this) {
        QRCodeType.Text -> colors.text
        QRCodeType.Link -> colors.link
        QRCodeType.Contact -> colors.contact
        QRCodeType.PhoneNumber -> colors.phone
        QRCodeType.Geolocation -> colors.geo
        QRCodeType.WiFi -> colors.wifi
    }
}

@Composable
fun QRCodeType.toBackgroundColor(): Color {
    val colors = MaterialTheme.colorScheme.appColors
    return when (this) {
        QRCodeType.Text -> colors.textBG
        QRCodeType.Link -> colors.linkBG
        QRCodeType.Contact -> colors.contactBG
        QRCodeType.PhoneNumber -> colors.phoneBG
        QRCodeType.Geolocation -> colors.geoBG
        QRCodeType.WiFi -> colors.wifiBG
    }
}
