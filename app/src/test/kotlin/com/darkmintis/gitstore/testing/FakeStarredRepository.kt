package com.darkmintis.gitstore.testing

import com.darkmintis.gitstore.core.data.local.db.entities.StarredRepo
import com.darkmintis.gitstore.core.domain.repository.StarredRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeStarredRepository(
    initial: List<StarredRepo> = emptyList()
) : StarredRepository {
    private val starred = MutableStateFlow(initial)

    override fun getAllStarred(): Flow<List<StarredRepo>> = starred

    override suspend fun isStarred(repoId: Long): Boolean =
        starred.value.any { it.repoId == repoId }

    override suspend fun isStarredSync(repoId: Long): Boolean =
        starred.value.any { it.repoId == repoId }

    override suspend fun syncStarredRepos(forceRefresh: Boolean): Result<Unit> =
        Result.success(Unit)

    override suspend fun updateStarredInstallStatus(
        repoId: Long,
        installed: Boolean,
        packageName: String?
    ) = Unit

    override suspend fun getLastSyncTime(): Long? = null

    override suspend fun needsSync(): Boolean = false
}
