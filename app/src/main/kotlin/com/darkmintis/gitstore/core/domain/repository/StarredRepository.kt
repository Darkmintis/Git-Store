package com.darkmintis.gitstore.core.domain.repository

import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.data.local.db.entities.StarredRepo

interface StarredRepository {
    fun getAllStarred(): Flow<List<StarredRepo>>
    suspend fun isStarred(repoId: Long): Boolean
    suspend fun isStarredSync(repoId: Long): Boolean

    suspend fun syncStarredRepos(forceRefresh: Boolean = false): Result<Unit>

    suspend fun updateStarredInstallStatus(repoId: Long, installed: Boolean, packageName: String?)

    suspend fun getLastSyncTime(): Long?
    suspend fun needsSync(): Boolean
}

