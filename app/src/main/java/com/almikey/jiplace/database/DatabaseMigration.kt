package com.almikey.jiplace.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // https://developer.android.com/reference/android/arch/persistence/room/ColumnInfo
        /*
        database.execSQL("ALTER TABLE pin "
                + " ADD COLUMN is_location_accurate INTEGER")
         */

    }
}