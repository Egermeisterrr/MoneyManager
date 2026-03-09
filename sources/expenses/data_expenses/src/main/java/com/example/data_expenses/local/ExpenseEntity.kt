package com.example.data_expenses.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
internal data class ExpenseEntity(
    @PrimaryKey val id: String,
    val category: String,
    val amount: Double,
    val comment: String,
    val timestampMillis: Long
)
