package com.lihan.qrnest.feature.preview.data

import com.lihan.qrnest.feature.preview.domain.ClipboardManager
import platform.UIKit.UIPasteboard

class IosClipboardManager : ClipboardManager {
    override fun copyText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
