package com.example.domain_expenses.use_case

import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.StatsPeriod
import java.util.Calendar

class FilterExpensesByPeriodUseCase {
    operator fun invoke(expenses: List<Expense>, period: StatsPeriod, nowMillis: Long): List<Expense> {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return expenses.filter { expense ->
            val expenseDate = Calendar.getInstance().apply { timeInMillis = expense.timestampMillis }
            when (period) {
                StatsPeriod.DAY -> isSameDay(now, expenseDate)
                StatsPeriod.WEEK -> isSameWeek(now, expenseDate)
                StatsPeriod.MONTH -> isSameMonth(now, expenseDate)
                StatsPeriod.YEAR -> isSameYear(now, expenseDate)
            }
        }
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.WEEK_OF_YEAR) == b.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
    }

    private fun isSameYear(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
    }
}
