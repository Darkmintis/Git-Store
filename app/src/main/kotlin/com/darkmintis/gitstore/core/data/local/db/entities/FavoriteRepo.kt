package com.darkmintis.gitstore.core.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_repos")
data class FavoriteRepo(
    @PrimaryKey
    val repoId: Long,

    val repoName: String,
    val repoOwner: String,
    val repoOwnerAvatarUrl: String,
    val repoDescription: String?,
    val primaryLanguage: String?,
    val repoUrl: String,

    val isInstalled: Boolean = false,
    val installedPackageName: String? = null,

    val latestVersion: String?,
    val latestReleaseUrl: String?,

    val addedAt: Long,
    val lastSyncedAt: Long,
)

