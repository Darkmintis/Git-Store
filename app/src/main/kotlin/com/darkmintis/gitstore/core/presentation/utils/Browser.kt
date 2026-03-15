package com.darkmintis.gitstore.core.presentation.utils

interface BrowserHelper {
    fun openUrl(
        url: String,
        useChooser: Boolean = false,
        onFailure: (error: String) -> Unit = { },
    )
}



