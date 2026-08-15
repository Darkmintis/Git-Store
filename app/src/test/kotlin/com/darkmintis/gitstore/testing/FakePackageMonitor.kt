package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.data.services.PackageMonitor
import com.darkmintis.gitstore.core.domain.model.SystemPackageInfo

class FakePackageMonitor(
    private var installedPackages: Set<String> = emptySet(),
    private val packageInfo: Map<String, SystemPackageInfo> = emptyMap()
) : PackageMonitor {
    fun setInstalled(packages: Set<String>) {
        installedPackages = packages
    }

    override suspend fun isPackageInstalled(packageName: String): Boolean =
        installedPackages.contains(packageName)

    override suspend fun getInstalledPackageInfo(packageName: String): SystemPackageInfo? =
        packageInfo[packageName]

    override suspend fun getAllInstalledPackageNames(): Set<String> = installedPackages
}
