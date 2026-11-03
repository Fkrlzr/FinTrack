package com.fintrack.data.repository

import androidx.room.withTransaction
import com.fintrack.data.backup.CsvBackup
import com.fintrack.data.local.CategoryEntity
import com.fintrack.data.local.FinTrackDatabase
import com.fintrack.data.local.TransactionEntity
import com.fintrack.data.local.WalletEntity
import com.fintrack.data.local.toDomain
import com.fintrack.data.local.toEntity
import com.fintrack.domain.model.Budget
import com.fintrack.domain.model.Category
import com.fintrack.domain.model.DEFAULT_WALLET_ID
import com.fintrack.domain.model.FALLBACK_CATEGORY_ID
import com.fintrack.domain.model.Frequency
import com.fintrack.domain.model.MonthTotals
import com.fintrack.domain.model.Recurring
import com.fintrack.domain.model.Transaction
import com.fintrack.domain.model.TransactionType
import com.fintrack.domain.model.Wallet
import com.fintrack.domain.model.WalletSummary
import com.fintrack.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class TransactionRepositoryImpl(private val db: FinTrackDatabase) : TransactionRepository {

    private val transactions = db.transactionDao()
    private val categories = db.categoryDao()
    private val wallets = db.walletDao()
    private val budgets = db.budgetDao()
    private val recurring = db.recurringDao()

    override fun observeTotals(startMillis: Long, endMillis: Long): Flow<MonthTotals> =
        combine(
            transactions.observeSum(TransactionType.INCOME.name, startMillis, endMillis),
            transactions.observeSum(TransactionType.EXPENSE.name, startMillis, endMillis),
        ) { income, expense -> MonthTotals(income, expense) }

    override fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactions.observeInRange(startMillis, endMillis).map { rows -> rows.map { it.toDomain() } }

    override fun search(term: String): Flow<List<Transaction>> =
        transactions.search(term).map { rows -> rows.map { it.toDomain() } }

    override fun observeExpenseByCategory(startMillis: Long, endMillis: Long): Flow<Map<String, Long>> =
        transactions.observeExpenseByCategory(startMillis, endMillis)
            .map { rows -> rows.associate { it.categoryId to it.expenseMinor } }

    override suspend fun upsert(transaction: Transaction) {
        transactions.upsert(transaction.toEntity())
    }

    override suspend fun delete(transaction: Transaction) {
        transactions.delete(transaction.toEntity())
    }

    override fun observeCategories(): Flow<List<Category>> =
        categories.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveCategory(category: Category) {
        categories.upsert(category.toEntity())
    }

    override suspend fun deleteCategory(id: String) {
        if (id == FALLBACK_CATEGORY_ID) return
        db.withTransaction {
            transactions.reassignCategory(id, FALLBACK_CATEGORY_ID)
            recurring.reassignCategory(id, FALLBACK_CATEGORY_ID)
            budgets.delete(id)
            categories.deleteById(id)
        }
    }

    override fun observeWallets(): Flow<List<WalletSummary>> =
        combine(wallets.observeAll(), transactions.observeWalletNet()) { rows, nets ->
            val netById = nets.associate { it.walletId to it.netMinor }
            rows.map { row ->
                WalletSummary(
                    wallet = row.toDomain(),
                    balanceMinor = row.openingMinor + (netById[row.id] ?: 0L),
                )
            }
        }

    override suspend fun saveWallet(wallet: Wallet) {
        wallets.upsert(wallet.toEntity())
    }

    override suspend fun deleteWallet(id: Long) {
        if (id == DEFAULT_WALLET_ID) return
        db.withTransaction {
            transactions.reassignWallet(id, DEFAULT_WALLET_ID)
            recurring.reassignWallet(id, DEFAULT_WALLET_ID)
            wallets.deleteById(id)
        }
    }

    override fun observeBudgets(): Flow<List<Budget>> =
        budgets.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveBudget(budget: Budget) {
        budgets.upsert(budget.toEntity())
    }

    override suspend fun deleteBudget(categoryId: String) {
        budgets.delete(categoryId)
    }

    override fun observeRecurring(): Flow<List<Recurring>> =
        recurring.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun saveRecurring(item: Recurring) {
        recurring.upsert(item.toEntity())
    }

    override suspend fun deleteRecurring(id: Long) {
        recurring.deleteById(id)
    }

    override suspend fun processDueRecurring(nowMillis: Long) {
        db.withTransaction {
            recurring.getDue(nowMillis).forEach { schedule ->
                var next = schedule.nextDateMillis
                while (next <= nowMillis) {
                    transactions.upsert(
                        TransactionEntity(
                            amountMinor = schedule.amountMinor,
                            type = schedule.type,
                            category = schedule.category,
                            walletId = schedule.walletId,
                            note = schedule.note,
                            dateMillis = next,
                        ),
                    )
                    next = nextOccurrence(next, Frequency.valueOf(schedule.frequency))
                }
                recurring.upsert(schedule.copy(nextDateMillis = next))
            }
        }
    }

    override suspend fun exportCsv(): String {
        val categoryLabels = categories.getAll().associate { it.id to it.label }
        val walletNames = wallets.getAll().associate { it.id to it.name }
        val rows = transactions.getAll().map { t ->
            CsvBackup.Row(
                dateTime = Instant.ofEpochMilli(t.dateMillis).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                type = TransactionType.valueOf(t.type),
                category = categoryLabels[t.category] ?: t.category,
                wallet = walletNames[t.walletId] ?: "",
                amountMinor = t.amountMinor,
                note = t.note,
            )
        }
        return CsvBackup.encode(rows)
    }

    override suspend fun importCsv(text: String): Int {
        val rows = CsvBackup.decode(text)
        return db.withTransaction {
            val knownCategories = categories.getAll().toMutableList()
            val knownWallets = wallets.getAll().toMutableList()

            rows.forEach { row ->
                val categoryId = when {
                    row.category.isBlank() -> FALLBACK_CATEGORY_ID
                    else -> knownCategories.firstOrNull { it.label.equals(row.category, ignoreCase = true) }?.id
                        ?: CategoryEntity(
                            id = "c${System.currentTimeMillis()}${knownCategories.size}",
                            label = row.category,
                            sortOrder = (knownCategories.maxOfOrNull { it.sortOrder } ?: 0) + 1,
                        ).also {
                            categories.upsert(it)
                            knownCategories.add(it)
                        }.id
                }
                val walletId = when {
                    row.wallet.isBlank() -> DEFAULT_WALLET_ID
                    else -> knownWallets.firstOrNull { it.name.equals(row.wallet, ignoreCase = true) }?.id
                        ?: WalletEntity(name = row.wallet, openingMinor = 0).let { created ->
                            val id = wallets.upsert(created)
                            knownWallets.add(created.copy(id = id))
                            id
                        }
                }
                transactions.upsert(
                    TransactionEntity(
                        amountMinor = row.amountMinor,
                        type = row.type.name,
                        category = categoryId,
                        walletId = walletId,
                        note = row.note,
                        dateMillis = row.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    ),
                )
            }
            rows.size
        }
    }

    private fun nextOccurrence(millis: Long, frequency: Frequency): Long {
        val zoned: ZonedDateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        val next = when (frequency) {
            Frequency.WEEKLY -> zoned.plusWeeks(1)
            Frequency.MONTHLY -> zoned.plusMonths(1)
        }
        return next.toInstant().toEpochMilli()
    }
}
