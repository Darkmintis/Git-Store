package com.darkmintis.gitstore.core.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "update_history")
data class UpdateHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val packageName: String,
    val appName: String,
    val repoOwner: String,
    val repoName: String,

    val fromVersion: String,
    val toVersion: String,

    val updatedAt: Long,
    val updateSource: InstallSource,
    val success: Boolean = true,
    val errorMessage: String? = null
)

