package com.darkmintis.gitstore.feature.details.presentation.model

import com.darkmintis.gitstore.R

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class LogResult {

    data object DownloadStarted : LogResult()
    data object UpdateStarted : LogResult()
    data object Downloaded : LogResult()

    data object InstallStarted : LogResult()
    data object Installed : LogResult()
    data object Updated : LogResult()

    data object Cancelled : LogResult()

    data object PreparingForAppManager : LogResult()
    data object OpenedInAppManager : LogResult()

    data class Error(val message: String?) : LogResult()
    data class Info(val message: String) : LogResult()
}


@Composable
fun LogResult.asText(): String {
    return when (this) {
        LogResult.DownloadStarted ->
            stringResource(R.string.log_download_started)

        LogResult.Downloaded ->
            stringResource(R.string.log_downloaded)

        LogResult.InstallStarted ->
            stringResource(R.string.log_install_started)

        LogResult.Installed ->
            stringResource(R.string.log_installed)

        LogResult.Updated ->
            stringResource(R.string.log_updated)

        LogResult.Cancelled ->
            stringResource(R.string.log_cancelled)

        LogResult.OpenedInAppManager ->
            stringResource(R.string.log_opened_appmanager)

        is LogResult.Error ->
            message?.let {
                stringResource(R.string.log_error_with_message, it)
            } ?: stringResource(R.string.log_error)

        is LogResult.Info -> message

        LogResult.PreparingForAppManager -> stringResource(R.string.log_prepare_appmanager)
        LogResult.UpdateStarted -> stringResource(R.string.log_update_started)
    }
}




