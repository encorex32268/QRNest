package com.lihan.qrnest.core.presentation.util

interface ExternalAppLauncher {
    fun openUrl(url: String)
    fun dialPhone(phone: String)
    fun openGeo(latLng: String)
    fun insertContact(name: String, email: String, phone: String)
    fun shareText(text: String)
    fun openAppSettings()
    fun openWifiSettings()
}
