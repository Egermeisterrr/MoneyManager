package com.example.feature_expenses.mvi

import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.StatsPeriod
import com.example.domain_expenses.use_case.AddExpenseUseCase
import com.example.domain_expenses.use_case.DeleteExpenseUseCase
import com.example.domain_expenses.use_case.FilterExpensesByPeriodUseCase
import com.example.domain_expenses.use_case.GetExpensesUseCase
import com.example.domain_expenses.use_case.UpdateExpenseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ExpensesContainer(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val filterExpensesByPeriodUseCase: FilterExpensesByPeriodUseCase
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(ExpensesState())
    internal val state: StateFlow<ExpensesState> = _state.asStateFlow()

    init {
        scope.launch { refreshExpenses() }
    }

    internal fun dispatch(intent: ExpensesIntent) {
        scope.launch {
            when (intent) {
                is ExpensesIntent.SelectPeriod -> selectPeriod(intent.period)
                ExpensesIntent.OpenAddExpenseDialog -> openAddDialog()
                is ExpensesIntent.OpenEditExpenseDialog -> openEditDialog(intent.expenseId)
                ExpensesIntent.CloseAddExpenseDialog -> resetDialog()
                is ExpensesIntent.SelectCategory -> {
                    setState { copy(selectedCategory = intent.category, isCategoryError = false) }
                }
                is ExpensesIntent.ChangeAmount -> {
                    setState { copy(amountInput = intent.amount, isAmountError = false) }
                }
                is ExpensesIntent.ChangeComment -> {
                    setState { copy(commentInput = intent.comment) }
                }
                ExpensesIntent.SubmitExpense -> submitExpense()
                is ExpensesIntent.RequestDeleteExpense -> {
                    setState {
                        copy(
                            pendingDeleteExpenseId = intent.expenseId,
                            isDeleteDialogOpen = true
                        )
                    }
                }
                ExpensesIntent.ConfirmDeleteExpense -> confirmDeleteExpense()
                ExpensesIntent.DismissDeleteExpense -> {
                    setState {
                        copy(
                            pendingDeleteExpenseId = null,
                            isDeleteDialogOpen = false
                        )
                    }
                }
                ExpensesIntent.UndoDeleteExpense -> undoDeleteExpense()
                ExpensesIntent.ConsumeUndoDeleteEvent -> {
                    setState { copy(recentlyDeletedExpense = null) }
                }
            }
        }
    }

    private suspend fun refreshExpenses() {
        val loadedExpenses = getExpensesUseCase()
        val filtered = filterExpensesByPeriodUseCase(
            expenses = loadedExpenses,
            period = state.value.selectedPeriod,
            nowMillis = System.currentTimeMillis()
        )
        setState {
            copy(
                allExpenses = loadedExpenses,
                visibleExpenses = filtered
            )
        }
    }

    private fun selectPeriod(period: StatsPeriod) {
        val filtered = filterExpensesByPeriodUseCase(
            expenses = state.value.allExpenses,
            period = period,
            nowMillis = System.currentTimeMillis()
        )
        setState {
            copy(
                selectedPeriod = period,
                visibleExpenses = filtered
            )
        }
    }

    private suspend fun submitExpense() {
        val selectedCategory = state.value.selectedCategory
        val amount = state.value.amountInput.toDoubleOrNull()

        val hasCategoryError = selectedCategory == null
        val hasAmountError = amount == null || amount <= 0

        if (hasCategoryError || hasAmountError) {
            setState {
                copy(
                    isCategoryError = hasCategoryError,
                    isAmountError = hasAmountError
                )
            }
            return
        }

        val editingExpenseId = state.value.editingExpenseId
        val updatedComment = state.value.commentInput.trim()
        if (editingExpenseId == null) {
            addExpenseUseCase(
                Expense(
                    id = UUID.randomUUID().toString(),
                    category = selectedCategory,
                    amount = amount,
                    comment = updatedComment,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        } else {
            val currentExpense = state.value.allExpenses.firstOrNull { it.id == editingExpenseId }
                ?: return
            updateExpenseUseCase(
                currentExpense.copy(
                    category = selectedCategory,
                    amount = amount,
                    comment = updatedComment
                )
            )
        }
        resetDialog()
        refreshExpenses()
    }

    private suspend fun confirmDeleteExpense() {
        val expenseId = state.value.pendingDeleteExpenseId ?: return
        val deletedExpense = state.value.allExpenses.firstOrNull { it.id == expenseId }
        deleteExpenseUseCase(expenseId)
        setState {
            copy(
                pendingDeleteExpenseId = null,
                isDeleteDialogOpen = false,
                recentlyDeletedExpense = deletedExpense,
                undoDeleteEventId = undoDeleteEventId + 1
            )
        }
        refreshExpenses()
    }

    private suspend fun undoDeleteExpense() {
        val deletedExpense = state.value.recentlyDeletedExpense ?: return
        addExpenseUseCase(deletedExpense)
        setState { copy(recentlyDeletedExpense = null) }
        refreshExpenses()
    }

    private fun openAddDialog() {
        setState {
            copy(
                isAddDialogOpen = true,
                editingExpenseId = null,
                selectedCategory = null,
                amountInput = "",
                commentInput = "",
                isCategoryError = false,
                isAmountError = false
            )
        }
    }

    private fun openEditDialog(expenseId: String) {
        val expense = state.value.allExpenses.firstOrNull { it.id == expenseId } ?: return
        setState {
            copy(
                isAddDialogOpen = true,
                editingExpenseId = expense.id,
                selectedCategory = expense.category,
                amountInput = expense.amount.toString(),
                commentInput = expense.comment,
                isCategoryError = false,
                isAmountError = false
            )
        }
    }

    private fun resetDialog() {
        setState {
            copy(
                isAddDialogOpen = false,
                editingExpenseId = null,
                selectedCategory = null,
                amountInput = "",
                commentInput = "",
                isCategoryError = false,
                isAmountError = false
            )
        }
    }

    private inline fun setState(transform: ExpensesState.() -> ExpensesState) {
        _state.value = _state.value.transform()
    }
}
