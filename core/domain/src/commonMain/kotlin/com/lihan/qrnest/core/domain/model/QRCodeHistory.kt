package com.lihan.qrnest.core.domain.model

data class QRCodeHistory(
    val id: Long? = null,
    val title: String? = null,
    val type: Int,
    val content: String,
    val createdAt: Long,
    val isGenerated: Boolean,
    val isFavorite: Boolean
)
