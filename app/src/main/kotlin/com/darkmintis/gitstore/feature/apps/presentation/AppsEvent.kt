package com.darkmintis.gitstore.feature.apps.presentation

sealed interface AppsEvent {
    data class ShowError(val message: String) : AppsEvent
    data class ShowSuccess(val message: String) : AppsEvent
    data class NavigateToRepo(val repoId: Long) : AppsEvent
}

