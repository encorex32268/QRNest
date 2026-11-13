package com.lihan.qrnest.feature.history.presentation

sealed interface ScanHistoryEvent {
    data class NavigateToPreview(val id: Long) : ScanHistoryEvent
    data class ShareQRCode(val content: String) : ScanHistoryEvent
}
