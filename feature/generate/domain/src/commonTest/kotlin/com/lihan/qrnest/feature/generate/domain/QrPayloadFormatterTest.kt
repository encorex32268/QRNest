package com.lihan.qrnest.feature.generate.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.lihan.qrnest.core.domain.model.QRCodeType
import kotlin.test.Test

class QrPayloadFormatterTest {

    @Test
    fun `format text preserves unicode emoji and whitespace`() {
        val raw = "  Hello 世界 🚀\nLine 2  "
        val formatted = QrPayloadFormatter.format(
            qrCodeType = QRCodeType.Text,
            first = raw
        )
        assertThat(formatted).isEqualTo(raw)
    }

    @Test
    fun `format link returns input url`() {
        val url = "https://example.com/path?q=測試&lang=zh-TW"
        val formatted = QrPayloadFormatter.format(
            qrCodeType = QRCodeType.Link,
            first = url
        )
        assertThat(formatted).isEqualTo(url)
    }

    @Test
    fun `format phone number prepends tel scheme without duplicating`() {
        assertThat(
            QrPayloadFormatter.format(QRCodeType.PhoneNumber, "+886912345678")
        ).isEqualTo("tel:+886912345678")

        assertThat(
            QrPayloadFormatter.format(QRCodeType.PhoneNumber, "TEL:+886912345678")
        ).isEqualTo("tel:+886912345678")
    }

    @Test
    fun `format geolocation handles valid coordinates and falls back to zero on malformed inputs`() {
        assertThat(
            QrPayloadFormatter.format(
                qrCodeType = QRCodeType.Geolocation,
                first = "25.033964",
                second = "121.564468"
            )
        ).isEqualTo("25.033964,121.564468")

        assertThat(
            QrPayloadFormatter.format(
                qrCodeType = QRCodeType.Geolocation,
                first = "not-a-number",
                second = "NaN"
            )
        ).isEqualTo("0.0,0.0")

        assertThat(
            QrPayloadFormatter.format(
                qrCodeType = QRCodeType.Geolocation,
                first = "Infinity",
                second = "-Infinity"
            )
        ).isEqualTo("0.0,0.0")
    }

    @Test
    fun `format contact joins three lines with newlines`() {
        val formatted = QrPayloadFormatter.format(
            qrCodeType = QRCodeType.Contact,
            first = "王小明 🎉",
            second = "ming@example.com",
            third = "+886900000000"
        )
        assertThat(formatted).isEqualTo("王小明 🎉\nming@example.com\n+886900000000")
    }

    @Test
    fun `format wifi outputs multiline SSID password and encryption format`() {
        val formatted = QrPayloadFormatter.format(
            qrCodeType = QRCodeType.WiFi,
            first = "Home_WiFi",
            second = "s3cr3t!@#",
            third = "WPA2"
        )
        assertThat(formatted).isEqualTo(
            "SSID: Home_WiFi\nPassword: s3cr3t!@#\nEncryption type: WPA2"
        )
    }

    @Test
    fun `isInputValid checks required field counts per QRCodeType`() {
        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Text, "")).isFalse()
        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Text, "hi")).isTrue()

        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Geolocation, "25.0", "")).isFalse()
        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Geolocation, "", "121.5")).isFalse()
        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Geolocation, "25.0", "121.5")).isTrue()

        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Contact, "A", "B", "")).isFalse()
        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.Contact, "A", "B", "C")).isTrue()

        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.WiFi, "S", "", "WPA")).isFalse()
        assertThat(QrPayloadFormatter.isInputValid(QRCodeType.WiFi, "S", "P", "WPA")).isTrue()
    }
}
