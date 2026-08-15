package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp
import com.darkmintis.gitstore.feature.apps.domain.repository.AppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppsRepository(
    initial: List<InstalledApp> = emptyList()
) : AppsRepository {
    private val apps = MutableStateFlow(initial)
    var openCalls = 0

    override suspend fun getApps(): Flow<List<InstalledApp>> = apps

    override suspend fun openApp(
        installedApp: InstalledApp,
        onCantLaunchApp: () -> Unit
    ) {
        openCalls++
    }
}
