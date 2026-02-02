package com.darkmintis.gitstore.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.darkmintis.gitstore.core.data.local.db.dao.FavoriteRepoDao
import com.darkmintis.gitstore.core.data.local.db.dao.InstalledAppDao
import com.darkmintis.gitstore.core.data.local.db.dao.StarredRepoDao
import com.darkmintis.gitstore.core.data.local.db.dao.UpdateHistoryDao
import com.darkmintis.gitstore.core.data.local.db.entities.FavoriteRepo
import com.darkmintis.gitstore.core.data.local.db.entities.InstalledApp
import com.darkmintis.gitstore.core.data.local.db.entities.StarredRepo
import com.darkmintis.gitstore.core.data.local.db.entities.UpdateHistory

@Database(
    entities = [
        InstalledApp::class,
        FavoriteRepo::class,
        UpdateHistory::class,
        StarredRepo::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract val installedAppDao: InstalledAppDao
    abstract val favoriteRepoDao: FavoriteRepoDao
    abstract val updateHistoryDao: UpdateHistoryDao
    abstract val starredReposDao: StarredRepoDao
}

