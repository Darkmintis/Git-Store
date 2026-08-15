package com.darkmintis.gitstore.core.presentation.utils

import android.content.Context
import androidx.annotation.StringRes

class AndroidStringProvider(
    private val context: Context
) : StringProvider {
    override fun getString(@StringRes resId: Int): String =
        context.getString(resId)

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String =
        context.getString(resId, *formatArgs)
}
