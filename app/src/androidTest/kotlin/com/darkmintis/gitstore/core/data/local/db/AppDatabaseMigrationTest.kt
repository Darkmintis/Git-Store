package com.darkmintis.gitstore.core.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.darkmintis.gitstore.core.data.local.db.migrations.MIGRATION_1_2
import com.darkmintis.gitstore.core.data.local.db.migrations.MIGRATION_2_3
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val testDb = "migration-test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To3() {
        helper.createDatabase(testDb, 1).close()

        helper.runMigrationsAndValidate(
            testDb,
            3,
            true,
            MIGRATION_1_2,
            MIGRATION_2_3
        )
    }
}
