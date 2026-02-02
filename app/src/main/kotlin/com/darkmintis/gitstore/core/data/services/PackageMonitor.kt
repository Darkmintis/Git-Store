package com.darkmintis.gitstore.core.data.services

import com.darkmintis.gitstore.core.domain.model.SystemPackageInfo

interface PackageMonitor {
    suspend fun isPackageInstalled(packageName: String): Boolean

    suspend fun getInstalledPackageInfo(packageName: String): SystemPackageInfo?

    suspend fun getAllInstalledPackageNames(): Set<String>
}

