package com.example.domain_expenses.use_case

import com.example.domain_expenses.repository.ExpensesRepository

class DeleteExpenseUseCase(private val repository: ExpensesRepository) {
    suspend operator fun invoke(expenseId: String) {
        repository.deleteExpense(expenseId)
    }
}
