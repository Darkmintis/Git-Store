package com.darkmintis.gitstore.core.presentation.utils

import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import androidx.core.net.toUri

class AndroidBrowserHelper(
    private val context: Context
) : BrowserHelper {
    override fun openUrl(
        url: String,
        useChooser: Boolean,
        onFailure: (error: String) -> Unit
    ) {
        try {
            val baseIntent = Intent(Intent.ACTION_VIEW, url.toUri())

            if (baseIntent.resolveActivity(context.packageManager) == null) {
                onFailure("No application available to handle this link")
                return
            }

            val intent = if (useChooser) {
                Intent.createChooser(baseIntent, "Open with")
            } else {
                baseIntent
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            onFailure("No application available to handle this link")
        } catch (t: Throwable) {
            onFailure(t.message ?: "Failed to open link")
        }
    }

}

