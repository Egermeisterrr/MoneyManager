package com.example.domain_expenses

import com.example.api_expenses.Expense

interface ExpensesRepository {
    suspend fun getExpenses(): List<Expense>
    suspend fun addExpense(expense: Expense)
}
