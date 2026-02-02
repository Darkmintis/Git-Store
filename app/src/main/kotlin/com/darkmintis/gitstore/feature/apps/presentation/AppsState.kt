package com.darkmintis.gitstore.feature.apps.presentation

import com.darkmintis.gitstore.feature.apps.presentation.model.AppItem
import com.darkmintis.gitstore.feature.apps.presentation.model.UpdateAllProgress

data class AppsState(
    val apps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isUpdatingAll: Boolean = false,
    val updateAllProgress: UpdateAllProgress? = null,
    val updateAllButtonEnabled: Boolean = true
)

