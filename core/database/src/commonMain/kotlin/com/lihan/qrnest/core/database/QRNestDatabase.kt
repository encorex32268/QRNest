package com.lihan.qrnest.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [QRCodeHistoryEntity::class],
    version = 1
)
@ConstructedBy(QRNestDatabaseConstructor::class)
abstract class QRNestDatabase : RoomDatabase() {
    abstract val dao: QRCodeHistoryDao

    companion object {
        const val DB_NAME = "qrnest.db"
    }
}

@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object QRNestDatabaseConstructor : RoomDatabaseConstructor<QRNestDatabase> {
    override fun initialize(): QRNestDatabase
}
