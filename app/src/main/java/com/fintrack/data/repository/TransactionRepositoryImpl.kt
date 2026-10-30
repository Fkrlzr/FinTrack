package com.fintrack.data.repository

import com.fintrack.data.local.TransactionDao
import com.fintrack.data.local.toDomain
import com.fintrack.data.local.toEntity
import com.fintrack.domain.model.MonthTotals
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import com.fintrack.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao,
) : TransactionRepository {

    override fun observeBalance(): Flow<Long> = dao.observeBalance()

    override fun observeTotals(startMillis: Long, endMillis: Long): Flow<MonthTotals> =
        combine(
            dao.observeSum(TransactionType.INCOME.name, startMillis, endMillis),
            dao.observeSum(TransactionType.EXPENSE.name, startMillis, endMillis),
        ) { income, expense -> MonthTotals(income, expense) }

    override fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        dao.observeInRange(startMillis, endMillis).map { rows -> rows.map { it.toDomain() } }

    override suspend fun upsert(transaction: Transaction) {
        dao.upsert(transaction.toEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        dao.delete(transaction.toEntity())
    }
}
