package com.darkmintis.gitstore.core.presentation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class AndroidClipboardHelper(
    private val context: Context
) : ClipboardHelper {
    override fun copy(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}

