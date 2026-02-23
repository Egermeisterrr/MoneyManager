package com.example.feature_expenses.mvi

import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.ExpenseCategory
import com.example.domain_expenses.models.StatsPeriod
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

data class ExpensesState(
    val allExpenses: List<Expense> = emptyList(),
    val visibleExpenses: List<Expense> = emptyList(),
    val selectedPeriod: StatsPeriod = StatsPeriod.MONTH,
    val isAddDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false,
    val pendingDeleteExpenseId: String? = null,
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
    data class RequestDeleteExpense(val expenseId: String) : ExpensesIntent
    data object ConfirmDeleteExpense : ExpensesIntent
    data object DismissDeleteExpense : ExpensesIntent
}
