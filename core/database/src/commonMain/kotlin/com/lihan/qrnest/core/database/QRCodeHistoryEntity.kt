package com.lihan.qrnest.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "QRCodeHistoryEntity")
data class QRCodeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String? = null,
    val type: Int,
    val content: String,
    val createdAt: Long,
    val isGenerated: Boolean,
    val isFavorite: Boolean
)
