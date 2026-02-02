package com.darkmintis.gitstore.core.presentation.utils

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun ApplyAndroidSystemBars(isDarkTheme: Boolean?) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    val isDark = isDarkTheme ?: isSystemInDarkTheme()

    DisposableEffect(isDark) {
        activity.updateSystemBars(isDark)
        onDispose { }
    }
}

// androidMain
fun Activity.updateSystemBars(isDarkTheme: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val controller = WindowInsetsControllerCompat(window, window.decorView)

    controller.isAppearanceLightStatusBars = !isDarkTheme
    controller.isAppearanceLightNavigationBars = !isDarkTheme
}



