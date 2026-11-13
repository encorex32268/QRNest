package com.lihan.qrnest.core.data.repository

import com.lihan.qrnest.core.data.mapper.toDomain
import com.lihan.qrnest.core.data.mapper.toEntity
import com.lihan.qrnest.core.database.QRCodeHistoryDao
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomHistoryRepository(
    private val dao: QRCodeHistoryDao
) : HistoryRepository {

    override suspend fun upsertHistory(history: QRCodeHistory): Long {
        return dao.upsertHistory(history.toEntity())
    }

    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        dao.updateFavoriteStatus(id = id, isFavorite = isFavorite)
    }

    override fun getHistories(): Flow<List<QRCodeHistory>> {
        return dao.getHistories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id = id)
    }

    override fun getScannedHistories(): Flow<List<QRCodeHistory>> {
        return dao.getScannedHistories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getGeneratedHistories(): Flow<List<QRCodeHistory>> {
        return dao.getGeneratedHistories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteAllScanned() {
        dao.deleteAllScanned()
    }

    override suspend fun deleteAllGenerated() {
        dao.deleteAllGenerated()
    }

    override suspend fun getHistoryById(id: Long): QRCodeHistory? {
        return dao.getHistoryById(id = id)?.toDomain()
    }
}
