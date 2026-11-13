package com.lihan.qrnest.feature.preview.domain

import com.lihan.qrnest.core.domain.util.DataError
import com.lihan.qrnest.core.domain.util.EmptyResult

interface QrImageSaver {
    suspend fun saveQrCode(content: String): EmptyResult<DataError.Local>
}
