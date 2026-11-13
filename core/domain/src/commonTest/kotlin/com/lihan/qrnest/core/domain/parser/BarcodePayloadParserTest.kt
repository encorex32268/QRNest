package com.lihan.qrnest.core.domain.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.lihan.qrnest.core.domain.model.QRCodeType
import kotlin.test.Test

class BarcodePayloadParserTest {

    @Test
    fun `parses standard WIFI payload`() {
        val result = BarcodePayloadParser.parse("WIFI:S:HomeNet;T:WPA;P:s3cr3t;;")

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_WIFI)
        assertThat(result.content).isEqualTo("SSID: HomeNet\nPassword: s3cr3t\nEncryption type: WPA")
    }

    @Test
    fun `parses WIFI payload with escaped semicolon and unicode`() {
        val result = BarcodePayloadParser.parse("WIFI:S:Café\\;5G;T:WPA2;P:pass\\:123;;")

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_WIFI)
        assertThat(result.content).isEqualTo("SSID: Café;5G\nPassword: pass:123\nEncryption type: WPA2")
    }

    @Test
    fun `parses phone URI case-insensitively`() {
        val result = BarcodePayloadParser.parse("TEL:+1-800-555-0199")

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_PHONE)
        assertThat(result.content).isEqualTo("tel:+1-800-555-0199")
    }

    @Test
    fun `parses geo URI stripping query parameters`() {
        val result = BarcodePayloadParser.parse("geo:25.033964,121.564468?q=25.033964,121.564468")

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_GEO)
        assertThat(result.content).isEqualTo("25.033964,121.564468")
    }

    @Test
    fun `parses VCARD payload into name email and phone`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Alice Chen
            EMAIL;TYPE=INTERNET:alice@example.com
            TEL;TYPE=CELL:+886912345678
            END:VCARD
        """.trimIndent()

        val result = BarcodePayloadParser.parse(vcard)

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_CONTACT_INFO)
        assertThat(result.content).isEqualTo("Alice Chen\nalice@example.com\n+886912345678")
    }

    @Test
    fun `parses MECARD payload into name email and phone`() {
        val result = BarcodePayloadParser.parse("MECARD:N:Bob Lee;TEL:0912345678;EMAIL:bob@example.com;;")

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_CONTACT_INFO)
        assertThat(result.content).isEqualTo("Bob Lee\nbob@example.com\n0912345678")
    }

    @Test
    fun `parses https URL as link`() {
        val result = BarcodePayloadParser.parse("  https://example.com/path?x=1  ")

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_URL)
        assertThat(result.content).isEqualTo("https://example.com/path?x=1")
    }

    @Test
    fun `falls back to text for plain text and emoji`() {
        val raw = "Hello 🌍 QRNest!"
        val result = BarcodePayloadParser.parse(raw)

        assertThat(result.type).isEqualTo(QRCodeType.TYPE_TEXT)
        assertThat(result.content).isEqualTo(raw)
    }
}
