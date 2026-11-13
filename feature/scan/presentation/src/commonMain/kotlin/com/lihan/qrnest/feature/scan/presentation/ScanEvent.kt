package com.lihan.qrnest.feature.scan.presentation

sealed interface ScanEvent {
    data class NavigateToPreview(val id: Long) : ScanEvent
}
