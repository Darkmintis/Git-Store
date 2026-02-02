package com.darkmintis.gitstore.feature.settings.presentation

import com.darkmintis.gitstore.core.presentation.model.AppTheme
import com.darkmintis.gitstore.core.presentation.model.FontTheme

sealed interface SettingsAction {
    data object OnNavigateBackClick : SettingsAction
    data class OnThemeColorSelected(val themeColor: AppTheme) : SettingsAction
    data class OnAmoledThemeToggled(val enabled: Boolean) : SettingsAction
    data class OnDarkThemeChange(val isDarkTheme: Boolean?) : SettingsAction
    data object OnLogoutClick : SettingsAction
    data object OnLogoutConfirmClick : SettingsAction
    data object OnLogoutDismiss : SettingsAction
    data object OnHelpClick : SettingsAction
    data class OnFontThemeSelected(val fontTheme: FontTheme) : SettingsAction
}

