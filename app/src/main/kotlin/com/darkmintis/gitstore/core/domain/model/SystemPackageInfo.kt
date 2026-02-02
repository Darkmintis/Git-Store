package com.darkmintis.gitstore.core.domain.model

data class SystemPackageInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val isInstalled: Boolean
)

