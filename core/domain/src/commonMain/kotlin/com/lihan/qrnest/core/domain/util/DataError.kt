package com.lihan.qrnest.core.domain.util

sealed interface DataError : Error {
    enum class Local : DataError {
        DISK_FULL,
        FILE_NOT_FOUND,
        IMAGE_DECODE_FAILED,
        NO_QR_FOUND,
        PERMISSION_DENIED,
        UNKNOWN
    }
}
