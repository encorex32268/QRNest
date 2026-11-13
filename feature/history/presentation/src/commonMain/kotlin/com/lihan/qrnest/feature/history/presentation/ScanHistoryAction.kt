package com.lihan.qrnest.feature.history.presentation

sealed interface ScanHistoryAction {
    data class ItemClick(val id: Long?) : ScanHistoryAction
    data class ItemLongClick(val id: Long?) : ScanHistoryAction
    data class ItemFavoriteClick(val id: Long?, val isFavorite: Boolean) : ScanHistoryAction
    data object ShareClick : ScanHistoryAction
    data object DeleteClick : ScanHistoryAction
    data object DismissEditorBottomSheet : ScanHistoryAction
}
