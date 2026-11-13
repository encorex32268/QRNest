package com.lihan.qrnest.feature.generate.presentation.create

sealed interface CreateAction {
    data object BackClick : CreateAction
    data class OnFirstInputChanged(val value: String) : CreateAction
    data class OnSecondInputChanged(val value: String) : CreateAction
    data class OnThirdInputChanged(val value: String) : CreateAction
    data object GenerateButtonClick : CreateAction
}
