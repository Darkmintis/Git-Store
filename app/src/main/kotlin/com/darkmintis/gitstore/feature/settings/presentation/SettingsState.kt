package com.darkmintis.gitstore.feature.settings.presentation

import com.darkmintis.gitstore.core.presentation.model.AppTheme
import com.darkmintis.gitstore.core.presentation.model.FontTheme

data class SettingsState(
    val selectedThemeColor: AppTheme = AppTheme.OCEAN,
    val selectedFontTheme: FontTheme = FontTheme.CUSTOM,
    val isLogoutDialogVisible: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val isAmoledThemeEnabled: Boolean = false,
    val isDarkTheme: Boolean? = null,
    val isCheckingGitStoreUpdate: Boolean = false,
    val isGitStoreUpdateAvailable: Boolean = false,
    val latestGitStoreVersion: String? = null,
    val latestGitStoreDownloadUrl: String? = null,
    val gitStoreUpdateErrorMessage: String? = null,
)

