package com.lihan.qrnest.core.presentation.di

import com.lihan.qrnest.core.presentation.util.ExternalAppLauncher
import com.lihan.qrnest.core.presentation.util.IosExternalAppLauncher
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformPresentationModule: Module = module {
    singleOf(::IosExternalAppLauncher).bind<ExternalAppLauncher>()
}
