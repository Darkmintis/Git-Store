package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.presentation.utils.StringProvider

class FakeStringProvider(
    private val strings: Map<Int, String> = emptyMap()
) : StringProvider {
    override fun getString(resId: Int): String =
        strings[resId] ?: "string_$resId"

    override fun getString(resId: Int, vararg formatArgs: Any): String {
        val template = strings[resId] ?: "string_$resId"
        return try {
            template.format(*formatArgs)
        } catch (_: Exception) {
            "$template ${formatArgs.joinToString()}"
        }
    }
}
