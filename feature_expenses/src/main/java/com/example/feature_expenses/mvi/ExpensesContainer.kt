package com.example.feature_expenses.mvi

import com.example.api_expenses.Expense
import com.example.api_expenses.ExpenseCategory
import com.example.domain_expenses.AddExpenseUseCase
import com.example.domain_expenses.FilterExpensesByPeriodUseCase
import com.example.domain_expenses.GetExpensesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.api.MVIStore
import pro.respawn.flowmvi.dsl.store
import java.util.UUID

class ExpensesContainer(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val filterExpensesByPeriodUseCase: FilterExpensesByPeriodUseCase
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store: MVIStore<ExpensesIntent, ExpensesState, Nothing> = store(
        initial = ExpensesState(),
        scope = scope
    ) {
        suspend fun refreshExpenses() {
            val loadedExpenses = getExpensesUseCase()
            val filtered = filterExpensesByPeriodUseCase(
                expenses = loadedExpenses,
                period = state.selectedPeriod,
                nowMillis = System.currentTimeMillis()
            )
            updateState {
                copy(
                    allExpenses = loadedExpenses,
                    visibleExpenses = filtered
                )
            }
        }

        suspend fun submitExpense() {
            val selectedCategory = state.selectedCategory
            val amount = state.amountInput.toDoubleOrNull()

            val hasCategoryError = selectedCategory == null
            val hasAmountError = amount == null || amount <= 0

            if (hasCategoryError || hasAmountError) {
                updateState {
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
                    comment = state.commentInput.trim(),
                    timestampMillis = System.currentTimeMillis()
                )
            )

            updateState {
                copy(
                    isAddDialogOpen = false,
                    selectedCategory = null,
                    amountInput = "",
                    commentInput = "",
                    isCategoryError = false,
                    isAmountError = false
                )
            }
            refreshExpenses()
        }

        init {
            refreshExpenses()
        }

        reduce { intent ->
            when (intent) {
                is ExpensesIntent.SelectPeriod -> {
                    val filtered = filterExpensesByPeriodUseCase(
                        expenses = state.allExpenses,
                        period = intent.period,
                        nowMillis = System.currentTimeMillis()
                    )
                    updateState {
                        copy(
                            selectedPeriod = intent.period,
                            visibleExpenses = filtered
                        )
                    }
                }

                ExpensesIntent.OpenAddExpenseDialog -> {
                    updateState { copy(isAddDialogOpen = true) }
                }

                ExpensesIntent.CloseAddExpenseDialog -> {
                    updateState {
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

                is ExpensesIntent.SelectCategory -> {
                    updateState { copy(selectedCategory = intent.category, isCategoryError = false) }
                }

                is ExpensesIntent.ChangeAmount -> {
                    updateState { copy(amountInput = intent.amount, isAmountError = false) }
                }

                is ExpensesIntent.ChangeComment -> {
                    updateState { copy(commentInput = intent.comment) }
                }

                ExpensesIntent.SubmitExpense -> submitExpense()
            }
        }
    }

    val state: StateFlow<ExpensesState> = store.state

    fun dispatch(intent: ExpensesIntent) {
        store.intent(intent)
    }
}
