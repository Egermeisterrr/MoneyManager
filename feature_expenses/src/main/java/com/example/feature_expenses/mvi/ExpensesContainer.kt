package com.example.feature_expenses.mvi

import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.StatsPeriod
import com.example.domain_expenses.use_case.AddExpenseUseCase
import com.example.domain_expenses.use_case.FilterExpensesByPeriodUseCase
import com.example.domain_expenses.use_case.GetExpensesUseCase
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
    private val filterExpensesByPeriodUseCase: FilterExpensesByPeriodUseCase
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(ExpensesState())
    val state: StateFlow<ExpensesState> = _state.asStateFlow()

    init {
        scope.launch { refreshExpenses() }
    }

    fun dispatch(intent: ExpensesIntent) {
        scope.launch {
            when (intent) {
                is ExpensesIntent.SelectPeriod -> selectPeriod(intent.period)
                ExpensesIntent.OpenAddExpenseDialog -> setState { copy(isAddDialogOpen = true) }
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

        addExpenseUseCase(
            Expense(
                id = UUID.randomUUID().toString(),
                category = selectedCategory,
                amount = amount,
                comment = state.value.commentInput.trim(),
                timestampMillis = System.currentTimeMillis()
            )
        )
        resetDialog()
        refreshExpenses()
    }

    private fun resetDialog() {
        setState {
            copy(
                isAddDialogOpen = false,
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
