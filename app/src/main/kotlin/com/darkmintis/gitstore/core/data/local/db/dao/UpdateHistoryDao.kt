package com.darkmintis.gitstore.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.darkmintis.gitstore.core.data.local.db.entities.UpdateHistory

@Dao
interface UpdateHistoryDao {
    @Query("SELECT * FROM update_history ORDER BY updatedAt DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<UpdateHistory>>

    @Query("SELECT * FROM update_history WHERE packageName = :packageName ORDER BY updatedAt DESC")
    fun getHistoryForApp(packageName: String): Flow<List<UpdateHistory>>

    @Insert
    suspend fun insertHistory(history: UpdateHistory)

    @Query("DELETE FROM update_history WHERE updatedAt < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)
}

