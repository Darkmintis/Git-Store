package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.presentation.utils.BrowserHelper
import com.darkmintis.gitstore.core.presentation.utils.ClipboardHelper

class FakeBrowserHelper : BrowserHelper {
    val openedUrls = mutableListOf<String>()

    override fun openUrl(
        url: String,
        useChooser: Boolean,
        onFailure: (error: String) -> Unit
    ) {
        openedUrls += url
    }
}

class FakeClipboardHelper : ClipboardHelper {
    var lastLabel: String? = null
    var lastText: String? = null

    override fun copy(label: String, text: String) {
        lastLabel = label
        lastText = text
    }
}
