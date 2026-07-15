package com.darkmintis.gitstore.feature.apps.presentation.model

data class UpdateAllProgress(
    val current: Int,
    val total: Int,
    val currentAppName: String,
    val succeeded: Int = 0,
    val failed: Int = 0,
)

