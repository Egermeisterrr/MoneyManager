package com.example.feature_expenses.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.ExpenseCategory
import com.example.domain_expenses.models.StatsPeriod
import com.example.feature_expenses.R
import com.example.feature_expenses.mvi.ExpensesContainer
import com.example.feature_expenses.mvi.ExpensesIntent
import com.example.feature_expenses.mvi.ExpensesState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CategoryPalette = mapOf(
    ExpenseCategory.FOOD to Color(0xFFFF7043),
    ExpenseCategory.TRANSPORT to Color(0xFF29B6F6),
    ExpenseCategory.BILLS to Color(0xFF66BB6A),
    ExpenseCategory.ENTERTAINMENT to Color(0xFFEC407A),
    ExpenseCategory.SHOPPING to Color(0xFFAB47BC),
    ExpenseCategory.HEALTH to Color(0xFFFFCA28),
    ExpenseCategory.OTHER to Color(0xFF78909C)
)

private val SCREEN_GRADIENT_COLORS = listOf(Color(0xFF0B111A), Color(0xFF101A28))
private val EMPTY_STATE_CARD_COLOR = Color(0xFF182232)
private val SUMMARY_CARD_COLOR = Color(0xFF182232)
private val EXPENSE_CARD_COLOR = Color(0xFF182232)
private val SWIPE_DELETE_BACKGROUND_COLOR = Color(0xFF3A1721)
private val SWIPE_DELETE_TEXT_COLOR = Color(0xFFFF9FA7)
private val EMPTY_PIE_COLOR = Color(0xFF2B3B54)
private const val SCREEN_PADDING = 16
private const val LIST_ITEM_SPACING = 12
private const val SWIPE_CORNER_RADIUS = 18
private const val SWIPE_HORIZONTAL_PADDING = 18
private const val PIE_CHART_SIZE = 200
private const val PIE_START_ANGLE = -90f
private const val FULL_CIRCLE_SWEEP = 360f
private const val CATEGORY_DROPDOWN_WIDTH = 0.92f

private data class AddExpenseDialogUiState(
    val selectedCategory: ExpenseCategory?,
    val amountInput: String,
    val commentInput: String,
    val isCategoryError: Boolean,
    val isAmountError: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesRoute(
    container: ExpensesContainer
) {
    val state by container.state.collectAsState()
    val onIntent: (ExpensesIntent) -> Unit = { intent -> container.dispatch(intent) }
    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.undo)
    val deleteMessage = stringResource(R.string.expense_deleted)

    LaunchedEffect(state.undoDeleteEventId) {
        if (state.undoDeleteEventId == 0L || state.recentlyDeletedExpense == null) return@LaunchedEffect
        when (
            snackbarHostState.showSnackbar(
                message = deleteMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long
            )
        ) {
            SnackbarResult.ActionPerformed -> onIntent(ExpensesIntent.UndoDeleteExpense)
            SnackbarResult.Dismissed -> onIntent(ExpensesIntent.ConsumeUndoDeleteEvent)
        }
    }

    ExpensesScreenContent(state = state, onIntent = onIntent, snackbarHostState = snackbarHostState)
    ExpensesDialogs(state = state, onIntent = onIntent)
}

@Composable
private fun ExpensesScreenContent(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onIntent(ExpensesIntent.OpenAddExpenseDialog) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_expense))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = SCREEN_GRADIENT_COLORS))
                .padding(padding)
                .padding(SCREEN_PADDING.dp)
        ) {
            ExpenseList(
                state = state,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun ExpenseList(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(LIST_ITEM_SPACING.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { SummarySection(expenses = state.visibleExpenses) }
        item {
            PeriodSwitcher(
                selected = state.selectedPeriod,
                onSelected = { onIntent(ExpensesIntent.SelectPeriod(it)) }
            )
        }
        item {
            Text(
                text = stringResource(R.string.expenses_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        items(state.visibleExpenses, key = { it.id }) { expense ->
            SwipeToDeleteExpenseItem(
                expense = expense,
                onDeleteRequest = { onIntent(ExpensesIntent.RequestDeleteExpense(expense.id)) },
                onEditRequest = { onIntent(ExpensesIntent.OpenEditExpenseDialog(expense.id)) }
            )
        }
        if (state.visibleExpenses.isEmpty()) {
            item { EmptyExpensesCard() }
        }
    }
}

@Composable
private fun SwipeToDeleteExpenseItem(
    expense: Expense,
    onDeleteRequest: () -> Unit,
    onEditRequest: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SWIPE_DELETE_BACKGROUND_COLOR, RoundedCornerShape(SWIPE_CORNER_RADIUS.dp))
                    .padding(horizontal = SWIPE_HORIZONTAL_PADDING.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    color = SWIPE_DELETE_TEXT_COLOR,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) {
        ExpenseItem(expense = expense, onClick = onEditRequest)
    }
}

@Composable
private fun EmptyExpensesCard() {
    Card(colors = CardDefaults.cardColors(containerColor = EMPTY_STATE_CARD_COLOR)) {
        Text(
            text = stringResource(R.string.no_expenses_for_period),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(SCREEN_PADDING.dp)
        )
    }
}

@Composable
private fun ExpensesDialogs(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit
) {
    if (state.isAddDialogOpen) {
        AddExpenseDialog(
            uiState = AddExpenseDialogUiState(
                selectedCategory = state.selectedCategory,
                amountInput = state.amountInput,
                commentInput = state.commentInput,
                isCategoryError = state.isCategoryError,
                isAmountError = state.isAmountError
            ),
            isEditMode = state.editingExpenseId != null,
            onDismiss = { onIntent(ExpensesIntent.CloseAddExpenseDialog) },
            onIntent = onIntent
        )
    }

    if (state.isDeleteDialogOpen) {
        ConfirmDeleteDialog(
            onDismiss = { onIntent(ExpensesIntent.DismissDeleteExpense) },
            onConfirm = { onIntent(ExpensesIntent.ConfirmDeleteExpense) }
        )
    }
}

@Composable
private fun SummarySection(expenses: List<Expense>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SUMMARY_CARD_COLOR)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.spending_by_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            PieChart(expenses = expenses)
            Spacer(modifier = Modifier.height(12.dp))
            Legend(expenses = expenses)
        }
    }
}

@Composable
private fun PieChart(expenses: List<Expense>) {
    val totals = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    val total = totals.values.sum().takeIf { it > 0 } ?: 1.0

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.size(PIE_CHART_SIZE.dp)) {
            var startAngle = PIE_START_ANGLE
            totals.forEach { (category, value) ->
                val sweep = ((value / total) * FULL_CIRCLE_SWEEP).toFloat()
                drawArc(
                    color = CategoryPalette.getValue(category),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
            if (totals.isEmpty()) {
                drawArc(
                    color = EMPTY_PIE_COLOR,
                    startAngle = 0f,
                    sweepAngle = FULL_CIRCLE_SWEEP,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )
            }
        }

        Text(
            text = NumberFormat.getCurrencyInstance().format(expenses.sumOf { it.amount }),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Legend(expenses: List<Expense>) {
    val totals = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    if (totals.isEmpty()) {
        Text(
            text = stringResource(R.string.add_first_expense_for_stats),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        totals.forEach { (category, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(CategoryPalette.getValue(category), CircleShape)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = category.localizedName())
                Spacer(modifier = Modifier.weight(1f))
                Text(text = NumberFormat.getCurrencyInstance().format(value), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PeriodSwitcher(
    selected: StatsPeriod,
    onSelected: (StatsPeriod) -> Unit
) {
    val periods = remember { StatsPeriod.entries.toList() }
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                selected = selected == period,
                onClick = { onSelected(period) }
            ) {
                Text(period.localizedName())
            }
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit
) {
    val appLocale = appLocale()
    val formatter = remember(appLocale) { SimpleDateFormat("MMM dd, HH:mm", appLocale) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EXPENSE_CARD_COLOR)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(expense.category.localizedName()) }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = NumberFormat.getCurrencyInstance().format(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (expense.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(expense.comment)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatter.format(Date(expense.timestampMillis)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    uiState: AddExpenseDialogUiState,
    isEditMode: Boolean,
    onDismiss: () -> Unit,
    onIntent: (ExpensesIntent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditMode) {
                    stringResource(R.string.edit_expense)
                } else {
                    stringResource(R.string.add_expense)
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedCategory?.localizedName().orEmpty(),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.category_required)) },
                        isError = uiState.isCategoryError,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(CATEGORY_DROPDOWN_WIDTH)
                    ) {
                        ExpenseCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.localizedName()) },
                                onClick = {
                                    onIntent(ExpensesIntent.SelectCategory(category))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.amountInput,
                    onValueChange = { onIntent(ExpensesIntent.ChangeAmount(it)) },
                    label = { Text(stringResource(R.string.amount_required)) },
                    isError = uiState.isAmountError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.commentInput,
                    onValueChange = { onIntent(ExpensesIntent.ChangeComment(it)) },
                    label = { Text(stringResource(R.string.comment_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onIntent(ExpensesIntent.SubmitExpense) }) {
                Text(
                    if (isEditMode) {
                        stringResource(R.string.save)
                    } else {
                        stringResource(R.string.add)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_expense_title)) },
        text = { Text(stringResource(R.string.delete_expense_confirmation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = SWIPE_DELETE_TEXT_COLOR)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ExpenseCategory.localizedName(): String = when (this) {
    ExpenseCategory.FOOD -> stringResource(R.string.category_food)
    ExpenseCategory.TRANSPORT -> stringResource(R.string.category_transport)
    ExpenseCategory.BILLS -> stringResource(R.string.category_bills)
    ExpenseCategory.ENTERTAINMENT -> stringResource(R.string.category_entertainment)
    ExpenseCategory.SHOPPING -> stringResource(R.string.category_shopping)
    ExpenseCategory.HEALTH -> stringResource(R.string.category_health)
    ExpenseCategory.OTHER -> stringResource(R.string.category_other)
}

@Composable
private fun StatsPeriod.localizedName(): String = when (this) {
    StatsPeriod.DAY -> stringResource(R.string.period_day)
    StatsPeriod.WEEK -> stringResource(R.string.period_week)
    StatsPeriod.MONTH -> stringResource(R.string.period_month)
    StatsPeriod.YEAR -> stringResource(R.string.period_year)
}

@Composable
private fun appLocale(): Locale {
    val configuration = LocalConfiguration.current
    val systemLocale = configuration.locales[0] ?: Locale.ENGLISH
    return if (systemLocale.language == "ru") Locale.forLanguageTag("ru") else Locale.ENGLISH
}
