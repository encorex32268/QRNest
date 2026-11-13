package com.lihan.qrnest.core.presentation.mapper

import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi
import com.lihan.qrnest.core.presentation.util.TimeFormat

fun QRCodeHistory.toUi(): QRCodeHistoryUi {
    return QRCodeHistoryUi(
        id = id,
        title = title,
        type = type,
        content = content,
        createdAt = createdAt,
        formattedCreatedAt = TimeFormat.toFormattedTime(createdAt),
        isGenerated = isGenerated,
        isFavorite = isFavorite
    )
}

fun QRCodeHistoryUi.toDomain(): QRCodeHistory {
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
