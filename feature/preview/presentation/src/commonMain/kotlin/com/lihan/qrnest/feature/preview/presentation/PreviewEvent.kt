package com.lihan.qrnest.feature.preview.presentation

import com.lihan.qrnest.core.presentation.util.UiText

sealed interface PreviewEvent {
    data object Back : PreviewEvent
    data object DataError : PreviewEvent
    data object SaveSucceed : PreviewEvent
    data class ShowError(val message: UiText) : PreviewEvent
    data class ShareQRCode(val content: String) : PreviewEvent
    data class OpenUrl(val url: String) : PreviewEvent
    data class DialPhone(val phone: String) : PreviewEvent
    data class OpenGeo(val latLng: String) : PreviewEvent
    data class InsertContact(val name: String, val email: String, val phone: String) : PreviewEvent
    data object OpenWifiSettings : PreviewEvent
}
