package com.lihan.qrnest.feature.preview.data.di

import com.lihan.qrnest.feature.preview.data.AndroidClipboardManager
import com.lihan.qrnest.feature.preview.data.AndroidQrImageSaver
import com.lihan.qrnest.feature.preview.domain.ClipboardManager
import com.lihan.qrnest.feature.preview.domain.QrImageSaver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPreviewDataModule: Module = module {
    single<ClipboardManager> { AndroidClipboardManager(androidContext()) }
    single<QrImageSaver> { AndroidQrImageSaver(androidContext()) }
}
