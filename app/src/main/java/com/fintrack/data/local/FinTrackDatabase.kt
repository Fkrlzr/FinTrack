package com.fintrack.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema JSON is exported to app/schemas. Every version bump needs a Migration here so user data
 * survives the update.
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        WalletEntity::class,
        BudgetEntity::class,
        RecurringEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun walletDao(): WalletDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao

    companion object {
        fun create(context: Context): FinTrackDatabase =
            Room.databaseBuilder(context, FinTrackDatabase::class.java, "fintrack.db")
                .addMigrations(MIGRATION_1_2)
                .addCallback(SeedCallback)
                .build()
    }
}

/** Rows every install needs. Runs once on a fresh database and once when upgrading from version 1. */
private val SEED_STATEMENTS = listOf(
    "INSERT OR IGNORE INTO categories (id, label, sortOrder) VALUES " +
        "('FOOD', 'Food', 1), ('TRANSPORT', 'Transport', 2), ('UTILITIES', 'Utilities', 3), " +
        "('SALARY', 'Salary', 4), ('SHOPPING', 'Shopping', 5), ('OTHERS', 'Others', 6)",
    "INSERT OR IGNORE INTO wallets (id, name, openingMinor) VALUES (1, 'Main wallet', 0)",
)

private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        SEED_STATEMENTS.forEach { db.execSQL(it) }
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `label` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `wallets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `openingMinor` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`categoryId` TEXT NOT NULL, `limitMinor` INTEGER NOT NULL, PRIMARY KEY(`categoryId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `recurring` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountMinor` INTEGER NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `walletId` INTEGER NOT NULL, `note` TEXT NOT NULL, `frequency` TEXT NOT NULL, `nextDateMillis` INTEGER NOT NULL)")
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `walletId` INTEGER NOT NULL DEFAULT 1")
        SEED_STATEMENTS.forEach { db.execSQL(it) }
    }
}
