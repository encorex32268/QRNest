package com.lihan.qrnest.feature.preview.presentation

sealed interface PreviewAction {
    data object BackClick : PreviewAction
    data object FavoriteClick : PreviewAction
    data object CopyClick : PreviewAction
    data object ShareClick : PreviewAction
    data object SaveClick : PreviewAction
    data object ContentPrimaryActionClick : PreviewAction
    data class OnTitleChanged(val value: String) : PreviewAction
}
