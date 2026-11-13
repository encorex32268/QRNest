package com.lihan.qrnest.feature.history.presentation.di

import com.lihan.qrnest.feature.history.presentation.ScanHistoryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyPresentationModule = module {
    viewModelOf(::ScanHistoryViewModel)
}
