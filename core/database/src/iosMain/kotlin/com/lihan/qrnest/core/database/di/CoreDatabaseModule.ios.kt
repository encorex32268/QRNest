package com.lihan.qrnest.core.database.di

import com.lihan.qrnest.core.database.DatabaseFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    singleOf(::DatabaseFactory)
}
