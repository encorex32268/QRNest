package com.lihan.qrnest.core.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class QRCodeHistoryUi(
    val id: Long? = null,
    val title: String? = null,
    val type: Int,
    val content: String,
    val createdAt: Long,
    val formattedCreatedAt: String,
    val isGenerated: Boolean,
    val isFavorite: Boolean
)
