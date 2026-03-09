package com.example.domain_expenses.use_case

import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.repository.ExpensesRepository

class UpdateExpenseUseCase(private val repository: ExpensesRepository) {
    suspend operator fun invoke(expense: Expense) {
        repository.updateExpense(expense)
    }
}
