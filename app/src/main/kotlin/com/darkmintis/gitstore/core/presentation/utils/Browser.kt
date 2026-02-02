package com.darkmintis.gitstore.core.presentation.utils

interface BrowserHelper {
    fun openUrl(
        url: String,
        onFailure: (error: String) -> Unit = { },
    )
}



