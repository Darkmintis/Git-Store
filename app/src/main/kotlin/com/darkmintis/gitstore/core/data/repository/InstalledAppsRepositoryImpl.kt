package com.darkmintis.gitstore.core.data.repository

import kotlinx.datetime.Clock as DateClock
import kotlinx.datetime.Instant as DateInstant

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.darkmintis.gitstore.core.data.local.db.AppDatabase
import com.darkmintis.gitstore.core.data.local.db.dao.InstalledAppDao
import com.darkmintis.gitstore.core.data.local.db.dao.UpdateHistoryDao
import com.darkmintis.gitstore.core.data.local.db.entities.InstallSource
import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp
import com.darkmintis.gitstore.core.data.local.db.entities.UpdateHistory
import com.darkmintis.gitstore.core.domain.repository.InstalledAppsRepository
import com.darkmintis.gitstore.core.data.services.Installer
import com.darkmintis.gitstore.feature.details.domain.repository.DetailsRepository

class InstalledAppsRepositoryImpl(
    private val database: AppDatabase,
    private val dao: InstalledAppDao,
    private val historyDao: UpdateHistoryDao,
    private val detailsRepository: DetailsRepository,
    private val installer: Installer,
) : InstalledAppsRepository {

    override suspend fun <R> executeInTransaction(block: suspend () -> R): R {
        return database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                block()
            }
        }
    }

    override fun getAllInstalledApps(): Flow<List<InstalledApp>> = dao.getAllInstalledApps()

    override fun getAppsWithUpdates(): Flow<List<InstalledApp>> = dao.getAppsWithUpdates()

    override fun getUpdateCount(): Flow<Int> = dao.getUpdateCount()

    override suspend fun getAppByPackage(packageName: String): InstalledApp? =
        dao.getAppByPackage(packageName)

    override suspend fun getAppByRepoId(repoId: Long): InstalledApp? =
        dao.getAppByRepoId(repoId)

    override suspend fun isAppInstalled(repoId: Long): Boolean =
        dao.getAppByRepoId(repoId) != null

    override suspend fun saveInstalledApp(app: InstalledApp) {
        dao.insertApp(app)
    }

    override suspend fun deleteInstalledApp(packageName: String) {
        dao.deleteByPackageName(packageName)
    }

    override suspend fun checkForUpdates(packageName: String): Boolean {
        val app = dao.getAppByPackage(packageName) ?: return false

        try {
            val latestRelease = detailsRepository.getLatestPublishedRelease(
                owner = app.repoOwner,
                repo = app.repoName,
                defaultBranch = ""
            )

            if (latestRelease != null) {
                val normalizedInstalledTag = normalizeVersion(app.installedVersion)
                val normalizedLatestTag = normalizeVersion(latestRelease.tagName)

                val installableAssets = latestRelease.assets.filter { asset ->
                    installer.isAssetInstallable(asset.name)
                }

                val primaryAsset = installer.choosePrimaryAsset(installableAssets)

                val isUpdateAvailable = normalizedInstalledTag != normalizedLatestTag

                Logger.d {
                    "Update check for ${app.appName}: currentTag=${app.installedVersion}, latestTag=${latestRelease.tagName}, " +
                            "currentCode=${app.installedVersionCode}, isUpdate=$isUpdateAvailable, " +
                            "primaryAsset=${primaryAsset?.name}"
                }

                dao.updateVersionInfo(
                    packageName = packageName,
                    available = isUpdateAvailable,
                    version = latestRelease.tagName,
                    assetName = primaryAsset?.name,
                    assetUrl = primaryAsset?.downloadUrl,
                    assetSize = primaryAsset?.size,
                    releaseNotes = latestRelease.description ?: "",
                    timestamp = DateClock.System.now().toEpochMilliseconds(),
                    latestVersionName = latestRelease.tagName,
                    latestVersionCode = null
                )

                return isUpdateAvailable
            }
        } catch (e: Exception) {
            Logger.e { "Failed to check updates for $packageName: ${e.message}" }
            dao.updateLastChecked(packageName, DateClock.System.now().toEpochMilliseconds())
        }

        return false
    }

    override suspend fun checkAllForUpdates() {
        val apps = dao.getAllInstalledApps().first()
        apps.forEach { app ->
            if (app.updateCheckEnabled) {
                try {
                    checkForUpdates(app.packageName)
                } catch (e: Exception) {
                    Logger.w { "Failed to check updates for ${app.packageName}: ${e.message}" }
                }
            }
        }
    }

    override suspend fun updateAppVersion(
        packageName: String,
        newTag: String,
        newAssetName: String,
        newAssetUrl: String,
        newVersionName: String,
        newVersionCode: Long
    ) {
        val app = dao.getAppByPackage(packageName) ?: return

        Logger.d {
            "Updating app version: $packageName from ${app.installedVersion} to $newTag"
        }

        historyDao.insertHistory(
            UpdateHistory(
                packageName = packageName,
                appName = app.appName,
                repoOwner = app.repoOwner,
                repoName = app.repoName,
                fromVersion = app.installedVersion,
                toVersion = newTag,
                updatedAt = DateClock.System.now().toEpochMilliseconds(),
                updateSource = InstallSource.THIS_APP,
                success = true
            )
        )

        dao.updateApp(
            app.copy(
                installedVersion = newTag,
                installedAssetName = newAssetName,
                installedAssetUrl = newAssetUrl,
                installedVersionName = newVersionName,
                installedVersionCode = newVersionCode,
                latestVersion = newTag,
                latestAssetName = newAssetName,
                latestAssetUrl = newAssetUrl,
                latestVersionName = newVersionName,
                latestVersionCode = newVersionCode,
                isUpdateAvailable = false,
                lastUpdatedAt = DateClock.System.now().toEpochMilliseconds(),
                lastCheckedAt = DateClock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun updateApp(app: InstalledApp) {
        dao.updateApp(app)
    }

    override suspend fun updatePendingStatus(packageName: String, isPending: Boolean) {
        val app = dao.getAppByPackage(packageName) ?: return
        dao.updateApp(app.copy(isPendingInstall = isPending))
    }

    private fun normalizeVersion(version: String): String {
        return version.removePrefix("v").removePrefix("V").trim()
    }
}

