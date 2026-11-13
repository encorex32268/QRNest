package com.lihan.qrnest.di

import com.lihan.qrnest.core.data.di.coreDataModule
import com.lihan.qrnest.core.database.di.coreDatabaseModule
import com.lihan.qrnest.core.presentation.di.corePresentationModule
import com.lihan.qrnest.feature.generate.presentation.di.generatePresentationModule
import com.lihan.qrnest.feature.history.presentation.di.historyPresentationModule
import com.lihan.qrnest.feature.preview.data.di.previewDataModule
import com.lihan.qrnest.feature.preview.presentation.di.previewPresentationModule
import com.lihan.qrnest.feature.scan.data.di.scanDataModule
import com.lihan.qrnest.feature.scan.presentation.di.scanPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDatabaseModule,
            coreDataModule,
            corePresentationModule,
            scanDataModule,
            scanPresentationModule,
            generatePresentationModule,
            historyPresentationModule,
            previewDataModule,
            previewPresentationModule
        )
    }
}
