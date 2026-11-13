package com.lihan.qrnest.feature.preview.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformPreviewDataModule: Module

val previewDataModule = module {
    includes(platformPreviewDataModule)
}
