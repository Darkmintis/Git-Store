package com.darkmintis.gitstore.feature.apps.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp

interface AppsRepository {
    suspend fun getApps(): Flow<List<InstalledApp>>
    suspend fun openApp(
        installedApp: InstalledApp,
        onCantLaunchApp : () -> Unit = { }
    )
}

