package com.lihan.qrnest.core.data.mapper

import com.lihan.qrnest.core.database.QRCodeHistoryEntity
import com.lihan.qrnest.core.domain.model.QRCodeHistory

fun QRCodeHistoryEntity.toDomain(): QRCodeHistory {
    return QRCodeHistory(
        id = id,
        title = title,
        type = type,
        content = content,
        createdAt = createdAt,
        isGenerated = isGenerated,
        isFavorite = isFavorite
    )
}

fun QRCodeHistory.toEntity(): QRCodeHistoryEntity {
    return QRCodeHistoryEntity(
        id = id,
        title = title,
        type = type,
        content = content,
        createdAt = createdAt,
        isGenerated = isGenerated,
        isFavorite = isFavorite
    )
}
