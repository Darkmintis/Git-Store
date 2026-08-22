package com.darkmintis.gitstore.core.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        safeExec(db, "ALTER TABLE installed_apps ADD COLUMN installedVersionName TEXT")
        safeExec(db, "ALTER TABLE installed_apps ADD COLUMN installedVersionCode INTEGER NOT NULL DEFAULT 0")
        safeExec(db, "ALTER TABLE installed_apps ADD COLUMN latestVersionName TEXT")
        safeExec(db, "ALTER TABLE installed_apps ADD COLUMN latestVersionCode INTEGER")
    }
}

private fun safeExec(db: SupportSQLiteDatabase, sql: String) {
    try { db.execSQL(sql) } catch (_: Exception) { }
}
