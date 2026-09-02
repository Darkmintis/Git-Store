package com.darkmintis.gitstore.feature.settings.presentation

import com.darkmintis.gitstore.core.domain.model.TranslationLanguage
import com.darkmintis.gitstore.core.presentation.model.AppTheme

sealed interface SettingsAction {
    data object OnNavigateBackClick : SettingsAction
    data class OnThemeColorSelected(val themeColor: AppTheme) : SettingsAction
    data class OnAmoledThemeToggled(val enabled: Boolean) : SettingsAction
    data class OnDarkThemeChange(val isDarkTheme: Boolean?) : SettingsAction
    data class OnTranslationLanguageSelected(val language: TranslationLanguage) : SettingsAction
    data class OnAutoTranslateToggled(val enabled: Boolean) : SettingsAction
    data object OnLogoutClick : SettingsAction
    data object OnLogoutConfirmClick : SettingsAction
    data object OnLogoutDismiss : SettingsAction
    data object OnGitHubSignInClick : SettingsAction
    data object OnOpenStarredReposClick : SettingsAction
    data object OnHelpClick : SettingsAction
    data object OnUpdateGitStoreClick : SettingsAction
    data class OnBrowserOpen(
        val url: String,
        val useChooser: Boolean = false
    ) : SettingsAction
}

