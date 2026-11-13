package com.lihan.qrnest.core.presentation.di

import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformPresentationModule: Module

val corePresentationModule = module {
    includes(platformPresentationModule)
}
