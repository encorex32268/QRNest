package com.lihan.qrnest.core.presentation.di

import com.lihan.qrnest.core.presentation.util.AndroidExternalAppLauncher
import com.lihan.qrnest.core.presentation.util.ExternalAppLauncher
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPresentationModule: Module = module {
    single<ExternalAppLauncher> { AndroidExternalAppLauncher(androidContext()) }
}
