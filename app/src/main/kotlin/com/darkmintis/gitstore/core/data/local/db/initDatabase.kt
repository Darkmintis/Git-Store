package com.darkmintis.gitstore.core.data.local.db

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import com.darkmintis.gitstore.core.data.local.db.migrations.MIGRATION_1_2
import com.darkmintis.gitstore.core.data.local.db.migrations.MIGRATION_2_3

fun initDatabase(context: Context): AppDatabase {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("github_store.db")
    return Room
        .databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
        )
        .build()
}

