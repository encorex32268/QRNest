package com.lihan.qrnest.feature.preview.data.di

import com.lihan.qrnest.feature.preview.data.IosClipboardManager
import com.lihan.qrnest.feature.preview.data.IosQrImageSaver
import com.lihan.qrnest.feature.preview.domain.ClipboardManager
import com.lihan.qrnest.feature.preview.domain.QrImageSaver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPreviewDataModule: Module = module {
    single<ClipboardManager> { IosClipboardManager() }
    single<QrImageSaver> { IosQrImageSaver() }
}
