package com.example.domain_expenses.models

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