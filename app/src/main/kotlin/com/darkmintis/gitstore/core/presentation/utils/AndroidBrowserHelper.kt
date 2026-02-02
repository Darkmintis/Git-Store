package com.darkmintis.gitstore.core.presentation.utils

import android.content.Intent
import android.content.Context
import androidx.core.net.toUri

class AndroidBrowserHelper(
    private val context: Context
) : BrowserHelper {
    override fun openUrl(
        url: String,
        onFailure: (error: String) -> Unit
    ) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

}

