package com.darkmintis.gitstore.core.presentation.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

fun isDynamicColorAvailable(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

@Composable
fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    if (!isDynamicColorAvailable()) return null

    val context = LocalContext.current
    return if (darkTheme) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
}


