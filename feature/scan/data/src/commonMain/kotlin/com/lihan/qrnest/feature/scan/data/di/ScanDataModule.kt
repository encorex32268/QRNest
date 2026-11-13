package com.lihan.qrnest.feature.scan.data.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformScanDataModule: Module

val scanDataModule = module {
    includes(platformScanDataModule)
}
