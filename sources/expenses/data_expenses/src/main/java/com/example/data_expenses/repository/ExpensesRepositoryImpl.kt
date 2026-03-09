package com.example.data_expenses.repository

import android.content.Context
import com.example.data_expenses.local.ExpenseEntity
import com.example.data_expenses.local.ExpensesDatabase
import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.ExpenseCategory
import com.example.domain_expenses.repository.ExpensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ExpensesRepositoryImpl(context: Context) : ExpensesRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val expenseDao = ExpensesDatabase.getInstance(context).expenseDao()

    init {
        runBlocking(Dispatchers.IO) {
            migrateSharedPrefsToRoomIfNeeded()
        }
    }

    override suspend fun getExpenses(): List<Expense> {
        return withContext(Dispatchers.IO) {
            expenseDao.getAll().map { it.toDomain() }
        }
    }

    override suspend fun addExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.insert(expense.toEntity())
        }
    }

    override suspend fun updateExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            expenseDao.update(expense.toEntity())
        }
    }

    override suspend fun deleteExpense(expenseId: String) {
        withContext(Dispatchers.IO) {
            expenseDao.deleteById(expenseId)
        }
    }

    private suspend fun migrateSharedPrefsToRoomIfNeeded() {
        if (prefs.getBoolean(KEY_ROOM_MIGRATION_DONE, false)) return

        if (expenseDao.count() > 0) {
            markMigrationDone()
            return
        }

        val encoded = prefs.getString(KEY_EXPENSES, null)
        if (!encoded.isNullOrBlank()) {
            val oldExpenses = runCatching {
                json.decodeFromString(ListSerializer(Expense.serializer()), encoded)
            }.getOrDefault(emptyList())

            if (oldExpenses.isNotEmpty()) {
                expenseDao.insertAll(oldExpenses.map { it.toEntity() })
            }
        }
        markMigrationDone()
    }

    private fun markMigrationDone() {
        prefs.edit().putBoolean(KEY_ROOM_MIGRATION_DONE, true).apply()
    }

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            category = category.name,
            amount = amount,
            comment = comment,
            timestampMillis = timestampMillis
        )
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            category = ExpenseCategory.valueOf(category),
            amount = amount,
            comment = comment,
            timestampMillis = timestampMillis
        )
    }

    private companion object {
        const val PREFS_NAME = "expenses_prefs"
        const val KEY_EXPENSES = "expenses"
        const val KEY_ROOM_MIGRATION_DONE = "room_migration_done"
    }
}
