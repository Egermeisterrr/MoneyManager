package com.example.domain_expenses.use_case

import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.repository.ExpensesRepository

class GetExpensesUseCase(private val repository: ExpensesRepository) {
    suspend operator fun invoke(): List<Expense> = repository.getExpenses()
}