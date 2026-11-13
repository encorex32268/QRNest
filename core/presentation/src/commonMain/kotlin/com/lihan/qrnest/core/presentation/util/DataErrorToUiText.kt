package com.lihan.qrnest.core.presentation.util

import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.error_disk_full
import com.lihan.qrnest.core.designsystem.resources.error_file_not_found
import com.lihan.qrnest.core.designsystem.resources.error_image_decode_failed
import com.lihan.qrnest.core.designsystem.resources.error_permission_denied
import com.lihan.qrnest.core.designsystem.resources.error_unknown
import com.lihan.qrnest.core.designsystem.resources.no_qrcode_found
import com.lihan.qrnest.core.domain.util.DataError

fun DataError.toUiText(): UiText {
    val resource = when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.FILE_NOT_FOUND -> Res.string.error_file_not_found
        DataError.Local.IMAGE_DECODE_FAILED -> Res.string.error_image_decode_failed
        DataError.Local.NO_QR_FOUND -> Res.string.no_qrcode_found
        DataError.Local.PERMISSION_DENIED -> Res.string.error_permission_denied
        DataError.Local.UNKNOWN -> Res.string.error_unknown
    }
    return UiText.StringResourceId(resource)
}
