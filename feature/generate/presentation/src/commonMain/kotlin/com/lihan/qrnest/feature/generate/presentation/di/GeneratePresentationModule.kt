package com.lihan.qrnest.feature.generate.presentation.di

import com.lihan.qrnest.feature.generate.presentation.create.CreateViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val generatePresentationModule = module {
    viewModel {
        CreateViewModel(
            historyRepository = get(),
            savedStateHandle = get()
        )
    }
}
