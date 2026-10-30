package com.fintrack

import android.app.Application
import com.fintrack.data.local.FinTrackDatabase
import com.fintrack.data.repository.TransactionRepositoryImpl
import com.fintrack.domain.repository.TransactionRepository

class FinTrackApp : Application() {
    // Lazy so the database opens on first query, not during process start.
    val repository: TransactionRepository by lazy {
        TransactionRepositoryImpl(FinTrackDatabase.create(this).transactionDao())
    }
}
