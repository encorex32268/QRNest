package com.lihan.qrnest.core.domain.repository

import com.lihan.qrnest.core.domain.model.QRCodeHistory
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun upsertHistory(history: QRCodeHistory): Long
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    fun getHistories(): Flow<List<QRCodeHistory>>
    suspend fun deleteById(id: Long)
    fun getScannedHistories(): Flow<List<QRCodeHistory>>
    fun getGeneratedHistories(): Flow<List<QRCodeHistory>>
    suspend fun deleteAllScanned()
    suspend fun deleteAllGenerated()
    suspend fun getHistoryById(id: Long): QRCodeHistory?
}
