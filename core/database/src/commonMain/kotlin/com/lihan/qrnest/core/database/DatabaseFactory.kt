package com.lihan.qrnest.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<QRNestDatabase>
}

fun DatabaseFactory.buildDatabase(): QRNestDatabase {
    return create()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
