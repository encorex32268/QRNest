package com.lihan.qrnest.feature.preview.presentation.di

import com.lihan.qrnest.feature.preview.presentation.PreviewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val previewPresentationModule = module {
    viewModelOf(::PreviewViewModel)
}
