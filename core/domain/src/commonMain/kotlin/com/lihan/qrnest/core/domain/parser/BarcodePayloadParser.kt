package com.lihan.qrnest.core.domain.parser

import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.model.ScannedBarcode

object BarcodePayloadParser {

    fun parse(rawValue: String): ScannedBarcode {
        val trimmed = rawValue.trim()
        return when {
            trimmed.startsWith("WIFI:", ignoreCase = true) -> parseWifi(trimmed)
            trimmed.startsWith("SSID:", ignoreCase = false) && trimmed.contains("\nPassword:", ignoreCase = true) -> {
                ScannedBarcode(type = QRCodeType.TYPE_WIFI, content = trimmed)
            }
            trimmed.startsWith("tel:", ignoreCase = true) -> parsePhone(trimmed)
            trimmed.startsWith("geo:", ignoreCase = true) -> parseGeo(trimmed)
            trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) -> parseVCard(trimmed)
            trimmed.startsWith("MECARD:", ignoreCase = true) -> parseMeCard(trimmed)
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> {
                ScannedBarcode(type = QRCodeType.TYPE_URL, content = trimmed)
            }
            else -> ScannedBarcode(type = QRCodeType.TYPE_TEXT, content = rawValue)
        }
    }

    private fun parseWifi(raw: String): ScannedBarcode {
        val body = raw.substringAfter(":", "")
        val fields = splitUnescaped(body, ';')
        var ssid = ""
        var password = ""
        var encryptionType = ""

        for (field in fields) {
            if (field.isEmpty()) continue
            val colonIndex = field.indexOf(':')
            if (colonIndex <= 0) continue
            val key = field.substring(0, colonIndex).uppercase()
            val value = unescapeWifiValue(field.substring(colonIndex + 1))
            when (key) {
                "S" -> ssid = value
                "P" -> password = value
                "T" -> encryptionType = value
            }
        }

        val content = buildString {
            append("SSID: ").append(ssid).append('\n')
            append("Password: ").append(password).append('\n')
            append("Encryption type: ").append(encryptionType)
        }
        return ScannedBarcode(type = QRCodeType.TYPE_WIFI, content = content)
    }

    private fun parsePhone(raw: String): ScannedBarcode {
        val number = raw.substring(4).trim()
        return ScannedBarcode(
            type = QRCodeType.TYPE_PHONE,
            content = "tel:$number"
        )
    }

    private fun parseGeo(raw: String): ScannedBarcode {
        val coordsPart = raw.substring(4)
            .substringBefore('?')
            .substringBefore(';')
            .trim()
        val parts = coordsPart.split(',')
        val lat = parts.getOrNull(0)?.trim() ?: ""
        val lng = parts.getOrNull(1)?.trim() ?: ""
        return ScannedBarcode(
            type = QRCodeType.TYPE_GEO,
            content = "$lat,$lng"
        )
    }

    private fun parseVCard(raw: String): ScannedBarcode {
        val lines = raw.lines()
        var fullName = ""
        var structuredName = ""
        val emails = mutableListOf<String>()
        val phones = mutableListOf<String>()

        for (line in lines) {
            val colonIndex = line.indexOf(':')
            if (colonIndex <= 0) continue
            val keyPart = line.substring(0, colonIndex).uppercase()
            val propertyName = keyPart.substringBefore(';')
            val value = line.substring(colonIndex + 1).trim()
            if (value.isEmpty()) continue

            when (propertyName) {
                "FN" -> fullName = value
                "N" -> {
                    val parts = value.split(';').filter { it.isNotBlank() }
                    structuredName = if (parts.size >= 2) {
                        "${parts[1]} ${parts[0]}".trim()
                    } else {
                        parts.joinToString(" ").trim()
                    }
                }
                "EMAIL" -> emails.add(value)
                "TEL" -> phones.add(value)
            }
        }

        val name = fullName.ifEmpty { structuredName }
        val content = listOf(
            name,
            emails.joinToString("\n"),
            phones.joinToString("\n")
        ).joinToString("\n")
        return ScannedBarcode(type = QRCodeType.TYPE_CONTACT_INFO, content = content)
    }

    private fun parseMeCard(raw: String): ScannedBarcode {
        val body = raw.substringAfter(":", "")
        val fields = splitUnescaped(body, ';')
        var name = ""
        val emails = mutableListOf<String>()
        val phones = mutableListOf<String>()

        for (field in fields) {
            if (field.isEmpty()) continue
            val colonIndex = field.indexOf(':')
            if (colonIndex <= 0) continue
            val key = field.substring(0, colonIndex).uppercase()
            val value = field.substring(colonIndex + 1).trim()
            if (value.isEmpty()) continue

            when (key) {
                "N" -> name = value.replace(",", " ").trim()
                "EMAIL" -> emails.add(value)
                "TEL" -> phones.add(value)
            }
        }

        val content = listOf(
            name,
            emails.joinToString("\n"),
            phones.joinToString("\n")
        ).joinToString("\n")
        return ScannedBarcode(type = QRCodeType.TYPE_CONTACT_INFO, content = content)
    }

    private fun splitUnescaped(input: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var escaped = false
        for (ch in input) {
            if (escaped) {
                current.append('\\').append(ch)
                escaped = false
            } else if (ch == '\\') {
                escaped = true
            } else if (ch == delimiter) {
                result.add(current.toString())
                current.clear()
            } else {
                current.append(ch)
            }
        }
        if (escaped) {
            current.append('\\')
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }

    private fun unescapeWifiValue(value: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < value.length) {
            if (value[i] == '\\' && i + 1 < value.length) {
                sb.append(value[i + 1])
                i += 2
            } else {
                sb.append(value[i])
                i++
            }
        }
        return sb.toString()
    }
}
