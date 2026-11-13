package com.lihan.qrnest.core.data.di

import com.lihan.qrnest.core.data.repository.RoomHistoryRepository
import com.lihan.qrnest.core.database.di.coreDatabaseModule
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    includes(coreDatabaseModule)
    singleOf(::RoomHistoryRepository).bind<HistoryRepository>()
}
