package com.fintrack

import android.app.Application
import com.fintrack.data.local.FinTrackDatabase
import com.fintrack.data.repository.TransactionRepositoryImpl
import com.fintrack.domain.repository.TransactionRepository
import com.fintrack.security.AppLockStore

class FinTrackApp : Application() {
    // Lazy so the database opens on first use, not during process start.
    val database: FinTrackDatabase by lazy { FinTrackDatabase.create(this) }
    val repository: TransactionRepository by lazy { TransactionRepositoryImpl(database) }
    val lockStore: AppLockStore by lazy { AppLockStore(this) }
}
