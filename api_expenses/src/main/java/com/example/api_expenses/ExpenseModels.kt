package com.example.api_expenses

import kotlinx.serialization.Serializable

@Serializable
enum class ExpenseCategory {
    FOOD,
    TRANSPORT,
    BILLS,
    ENTERTAINMENT,
    SHOPPING,
    HEALTH,
    OTHER
}

@Serializable
enum class StatsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

@Serializable
data class Expense(
    val id: String,
    val category: ExpenseCategory,
    val amount: Double,
    val comment: String,
    val timestampMillis: Long
)
