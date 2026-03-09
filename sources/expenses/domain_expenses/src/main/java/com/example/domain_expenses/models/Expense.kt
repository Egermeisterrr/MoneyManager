package com.example.domain_expenses.models

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val category: ExpenseCategory,
    val amount: Double,
    val comment: String,
    val timestampMillis: Long
)
