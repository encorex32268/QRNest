package com.lihan.qrnest.core.domain.model

enum class QRCodeType(val typeCode: Int) {
    Text(7),
    Link(8),
    Contact(1),
    PhoneNumber(4),
    Geolocation(10),
    WiFi(9);

    companion object {
        const val TYPE_CONTACT_INFO = 1
        const val TYPE_PHONE = 4
        const val TYPE_TEXT = 7
        const val TYPE_URL = 8
        const val TYPE_WIFI = 9
        const val TYPE_GEO = 10

        fun fromTypeCode(code: Int?): QRCodeType {
            return entries.find { it.typeCode == code } ?: Text
        }
    }
}
