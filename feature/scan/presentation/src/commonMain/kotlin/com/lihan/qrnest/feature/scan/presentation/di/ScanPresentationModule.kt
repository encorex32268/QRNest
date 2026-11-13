package com.lihan.qrnest.feature.scan.presentation.di

import com.lihan.qrnest.feature.scan.presentation.ScanViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val scanPresentationModule = module {
    viewModel {
        ScanViewModel(
            historyRepository = get(),
            qrImageScanner = get()
        )
    }
}
