package com.lihan.qrnest.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<QRNestDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(QRNestDatabase.DB_NAME)

        return Room.databaseBuilder<QRNestDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}
