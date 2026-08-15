package com.darkmintis.gitstore.feature.settings.presentation

import com.darkmintis.gitstore.core.presentation.model.AppTheme

data class SettingsState(
    val selectedThemeColor: AppTheme = AppTheme.OCEAN,
    val isLogoutDialogVisible: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val isAmoledThemeEnabled: Boolean = false,
    val isDarkTheme: Boolean? = null,
    val isCheckingGitStoreUpdate: Boolean = false,
    val isGitStoreUpdateAvailable: Boolean = false,
    val latestGitStoreVersion: String? = null,
    val latestGitStoreDownloadUrl: String? = null,
    val gitStoreUpdateErrorMessage: String? = null,
    val isDownloadingUpdate: Boolean = false,
    val updateDownloadProgress: Int? = null,
    val updateDownloadError: String? = null,
)

