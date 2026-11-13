package com.lihan.qrnest.core.database.di

import com.lihan.qrnest.core.database.DatabaseFactory
import com.lihan.qrnest.core.database.QRNestDatabase
import com.lihan.qrnest.core.database.buildDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDatabaseModule: Module

val coreDatabaseModule = module {
    includes(platformDatabaseModule)
    single { get<DatabaseFactory>().buildDatabase() }
    single { get<QRNestDatabase>().dao }
}
