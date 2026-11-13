package com.lihan.qrnest.feature.generate.domain

import com.lihan.qrnest.core.domain.model.QRCodeType

object QrPayloadFormatter {

    fun format(
        qrCodeType: QRCodeType,
        first: String,
        second: String = "",
        third: String = "",
    ): String {
        return when (qrCodeType) {
            QRCodeType.PhoneNumber -> {
                val trimmed = first.trim()
                if (trimmed.startsWith("tel:", ignoreCase = true)) {
                    "tel:${trimmed.substring(4)}"
                } else {
                    "tel:$trimmed"
                }
            }

            QRCodeType.Link,
            QRCodeType.Text -> first

            QRCodeType.Geolocation -> {
                val lat = first.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: 0.0
                val lng = second.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: 0.0
                "$lat,$lng"
            }

            QRCodeType.Contact -> "$first\n$second\n$third"

            QRCodeType.WiFi -> {
                """
                    SSID: $first
                    Password: $second
                    Encryption type: $third
                """.trimIndent()
            }
        }
    }

    fun isInputValid(
        qrCodeType: QRCodeType,
        first: String,
        second: String = "",
        third: String = "",
    ): Boolean {
        return when (qrCodeType) {
            QRCodeType.Text,
            QRCodeType.Link,
            QRCodeType.PhoneNumber -> first.isNotEmpty()

            QRCodeType.Geolocation -> first.isNotEmpty() && second.isNotEmpty()

            QRCodeType.Contact,
            QRCodeType.WiFi -> {
                first.isNotEmpty() && second.isNotEmpty() && third.isNotEmpty()
            }
        }
    }
}
