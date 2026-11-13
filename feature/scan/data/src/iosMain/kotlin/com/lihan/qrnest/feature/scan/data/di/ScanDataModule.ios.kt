package com.lihan.qrnest.feature.scan.data.di

import com.lihan.qrnest.feature.scan.data.VisionQrImageScanner
import com.lihan.qrnest.feature.scan.domain.QrImageScanner
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformScanDataModule: Module = module {
    single<QrImageScanner> { VisionQrImageScanner() }
}
