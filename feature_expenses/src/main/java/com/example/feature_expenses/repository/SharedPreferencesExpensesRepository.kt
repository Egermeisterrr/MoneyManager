package com.example.feature_expenses.repository

import android.content.Context
import com.example.api_expenses.Expense
import com.example.domain_expenses.ExpensesRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SharedPreferencesExpensesRepository(
    context: Context
) : ExpensesRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getExpenses(): List<Expense> {
        val encoded = prefs.getString(KEY_EXPENSES, null) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(Expense.serializer()), encoded)
        }.getOrDefault(emptyList())
    }

    override suspend fun addExpense(expense: Expense) {
        val current = getExpenses().toMutableList().apply { add(0, expense) }
        val encoded = json.encodeToString(ListSerializer(Expense.serializer()), current)
        prefs.edit().putString(KEY_EXPENSES, encoded).apply()
    }

    private companion object {
        const val PREFS_NAME = "expenses_prefs"
        const val KEY_EXPENSES = "expenses"
    }
}
