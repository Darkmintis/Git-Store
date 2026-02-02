package com.darkmintis.gitstore.feature.apps.presentation.model

import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp

data class AppItem(
    val installedApp: InstalledApp,
    val updateState: UpdateState = UpdateState.Idle,
    val downloadProgress: Int? = null,
    val error: String? = null
)

