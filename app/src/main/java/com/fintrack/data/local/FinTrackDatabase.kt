package com.fintrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Schema JSON is exported to app/schemas. When bumping [version], add an explicit Migration
 * (or an AutoMigration with the previous schema) so user data survives the update.
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        fun create(context: Context): FinTrackDatabase =
            Room.databaseBuilder(context, FinTrackDatabase::class.java, "fintrack.db").build()
    }
}
