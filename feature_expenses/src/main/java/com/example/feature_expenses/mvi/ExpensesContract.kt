package com.example.feature_expenses.mvi

import com.example.api_expenses.Expense
import com.example.api_expenses.ExpenseCategory
import com.example.api_expenses.StatsPeriod
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

data class ExpensesState(
    val allExpenses: List<Expense> = emptyList(),
    val visibleExpenses: List<Expense> = emptyList(),
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val isAddDialogOpen: Boolean = false,
    val selectedCategory: ExpenseCategory? = null,
    val amountInput: String = "",
    val commentInput: String = "",
    val isCategoryError: Boolean = false,
    val isAmountError: Boolean = false
) : MVIState

sealed interface ExpensesIntent : MVIIntent {
    data class SelectPeriod(val period: StatsPeriod) : ExpensesIntent
    data object OpenAddExpenseDialog : ExpensesIntent
    data object CloseAddExpenseDialog : ExpensesIntent
    data class SelectCategory(val category: ExpenseCategory) : ExpensesIntent
    data class ChangeAmount(val amount: String) : ExpensesIntent
    data class ChangeComment(val comment: String) : ExpensesIntent
    data object SubmitExpense : ExpensesIntent
}
