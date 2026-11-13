package com.lihan.qrnest.feature.preview.data

import android.content.ClipData
import android.content.Context
import com.lihan.qrnest.feature.preview.domain.ClipboardManager
import android.content.ClipboardManager as AndroidSystemClipboardManager

class AndroidClipboardManager(
    private val context: Context
) : ClipboardManager {

    private val clipboardManager by lazy {
        context.getSystemService(AndroidSystemClipboardManager::class.java)
    }

    override fun copyText(text: String) {
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("", text))
    }
}
