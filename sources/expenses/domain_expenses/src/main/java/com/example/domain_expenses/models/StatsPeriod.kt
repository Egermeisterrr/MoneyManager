package com.example.domain_expenses.models

import kotlinx.serialization.Serializable

@Serializable
enum class StatsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR
}
