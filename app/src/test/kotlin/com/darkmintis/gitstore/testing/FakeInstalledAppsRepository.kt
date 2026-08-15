package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.data.local.db.entities.InstallSource
import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp
import com.darkmintis.gitstore.core.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeInstalledAppsRepository(
    initial: List<InstalledApp> = emptyList()
) : InstalledAppsRepository {
    private val apps = MutableStateFlow(initial)
    val deleted = mutableListOf<String>()
    val updated = mutableListOf<InstalledApp>()

    override fun getAllInstalledApps(): Flow<List<InstalledApp>> = apps

    override fun getAppsWithUpdates(): Flow<List<InstalledApp>> =
        apps.map { list -> list.filter { it.isUpdateAvailable } }

    override fun getUpdateCount(): Flow<Int> =
        apps.map { list -> list.count { it.isUpdateAvailable } }

    override suspend fun getAppByPackage(packageName: String): InstalledApp? =
        apps.value.find { it.packageName == packageName }

    override suspend fun getAppByRepoId(repoId: Long): InstalledApp? =
        apps.value.find { it.repoId == repoId }

    override suspend fun isAppInstalled(repoId: Long): Boolean =
        apps.value.any { it.repoId == repoId }

    override suspend fun saveInstalledApp(app: InstalledApp) {
        apps.value = apps.value.filterNot { it.packageName == app.packageName } + app
    }

    override suspend fun deleteInstalledApp(packageName: String) {
        deleted += packageName
        apps.value = apps.value.filterNot { it.packageName == packageName }
    }

    override suspend fun checkForUpdates(packageName: String): Boolean = false

    override suspend fun checkAllForUpdates() = Unit

    override suspend fun updateAppVersion(
        packageName: String,
        newTag: String,
        newAssetName: String,
        newAssetUrl: String,
        newVersionName: String,
        newVersionCode: Long
    ) = Unit

    override suspend fun updateApp(app: InstalledApp) {
        updated += app
        apps.value = apps.value.filterNot { it.packageName == app.packageName } + app
    }

    override suspend fun updatePendingStatus(packageName: String, isPending: Boolean) = Unit

    override suspend fun <R> executeInTransaction(block: suspend () -> R): R = block()

    companion object {
        fun sampleApp(
            packageName: String = "com.example.app",
            repoId: Long = 1L,
            installedVersionName: String? = "1.0.0",
            isUpdateAvailable: Boolean = false
        ) = InstalledApp(
            packageName = packageName,
            repoId = repoId,
            repoName = "app",
            repoOwner = "owner",
            repoOwnerAvatarUrl = "https://example.com/a.png",
            repoDescription = "desc",
            primaryLanguage = "Kotlin",
            repoUrl = "https://github.com/owner/app",
            installedVersion = "v1.0.0",
            installedAssetName = "app.apk",
            installedAssetUrl = "https://example.com/app.apk",
            latestVersion = "v1.0.0",
            latestAssetName = "app.apk",
            latestAssetUrl = "https://example.com/app.apk",
            latestAssetSize = 1L,
            appName = "App",
            installSource = InstallSource.THIS_APP,
            installedAt = 0L,
            lastCheckedAt = 0L,
            lastUpdatedAt = 0L,
            isUpdateAvailable = isUpdateAvailable,
            systemArchitecture = "arm64-v8a",
            fileExtension = "apk",
            installedVersionName = installedVersionName,
            installedVersionCode = 1L,
            latestVersionName = installedVersionName,
            latestVersionCode = 1L
        )
    }
}
