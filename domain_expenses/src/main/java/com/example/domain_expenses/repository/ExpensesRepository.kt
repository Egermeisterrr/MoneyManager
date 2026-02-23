package com.example.domain_expenses.repository

import com.example.domain_expenses.models.Expense

interface ExpensesRepository {
    suspend fun getExpenses(): List<Expense>
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: String)
}
