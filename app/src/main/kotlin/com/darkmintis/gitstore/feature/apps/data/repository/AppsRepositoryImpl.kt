package com.darkmintis.gitstore.feature.apps.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp
import com.darkmintis.gitstore.core.domain.repository.InstalledAppsRepository
import com.darkmintis.gitstore.core.presentation.utils.AppLauncher
import com.darkmintis.gitstore.feature.apps.domain.repository.AppsRepository

class AppsRepositoryImpl(
    private val appLauncher: AppLauncher,
    private val appsRepository: InstalledAppsRepository
) : AppsRepository {
    override suspend fun getApps(): Flow<List<InstalledApp>> {
        return appsRepository.getAllInstalledApps()
    }

    override suspend fun openApp(
        installedApp: InstalledApp,
        onCantLaunchApp: () -> Unit
    ) {
        val canLaunch = appLauncher.canLaunchApp(installedApp)

        if (canLaunch) {
            appLauncher.launchApp(installedApp)
                .onFailure { error ->
                    Logger.e { "Failed to launch app: ${error.message}" }
                    onCantLaunchApp()
                }
        } else {
            onCantLaunchApp()
        }
    }
}

