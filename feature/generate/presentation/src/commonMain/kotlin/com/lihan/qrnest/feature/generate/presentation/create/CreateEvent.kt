package com.lihan.qrnest.feature.generate.presentation.create

sealed interface CreateEvent {
    data class NavigateToPreview(val id: Long) : CreateEvent
}
