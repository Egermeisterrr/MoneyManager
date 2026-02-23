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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain_expenses.models.Expense
import com.example.domain_expenses.models.ExpenseCategory
import com.example.domain_expenses.models.StatsPeriod
import com.example.feature_expenses.mvi.ExpensesContainer
import com.example.feature_expenses.mvi.ExpensesIntent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesRoute(
    container: ExpensesContainer
) {
    val state by container.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { container.dispatch(ExpensesIntent.OpenAddExpenseDialog) }) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5F7FF), Color(0xFFEFFAF5))
                    )
                )
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SummarySection(expenses = state.visibleExpenses)
                }

                item {
                    PeriodSwitcher(
                        selected = state.selectedPeriod,
                        onSelected = { container.dispatch(ExpensesIntent.SelectPeriod(it)) }
                    )
                }

                item {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(state.visibleExpenses, key = { it.id }) { expense ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                container.dispatch(ExpensesIntent.RequestDeleteExpense(expense.id))
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
                                    .background(Color(0xFFFFEBEE), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Delete",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    ) {
                        ExpenseItem(expense)
                    }
                }

                if (state.visibleExpenses.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f))
                        ) {
                            Text(
                                text = "No expenses for this period yet.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isAddDialogOpen) {
        AddExpenseDialog(
            selectedCategory = state.selectedCategory,
            amountInput = state.amountInput,
            commentInput = state.commentInput,
            isCategoryError = state.isCategoryError,
            isAmountError = state.isAmountError,
            onDismiss = { container.dispatch(ExpensesIntent.CloseAddExpenseDialog) },
            onCategorySelected = { container.dispatch(ExpensesIntent.SelectCategory(it)) },
            onAmountChanged = { container.dispatch(ExpensesIntent.ChangeAmount(it)) },
            onCommentChanged = { container.dispatch(ExpensesIntent.ChangeComment(it)) },
            onAddClick = { container.dispatch(ExpensesIntent.SubmitExpense) }
        )
    }

    if (state.isDeleteDialogOpen) {
        ConfirmDeleteDialog(
            onDismiss = { container.dispatch(ExpensesIntent.DismissDeleteExpense) },
            onConfirm = { container.dispatch(ExpensesIntent.ConfirmDeleteExpense) }
        )
    }
}

@Composable
private fun SummarySection(expenses: List<Expense>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spending by category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            totals.forEach { (category, value) ->
                val sweep = ((value / total) * 360f).toFloat()
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
                    color = Color(0xFFE0E0E0),
                    startAngle = 0f,
                    sweepAngle = 360f,
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
        Text("Add your first expense to see statistics.")
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
                Text(text = category.name.lowercase().replaceFirstChar { it.uppercase() })
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
                Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense) {
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = { Text(expense.category.name.lowercase().replaceFirstChar { it.uppercase() }) }
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

@Composable
private fun AddExpenseDialog(
    selectedCategory: ExpenseCategory?,
    amountInput: String,
    commentInput: String,
    isCategoryError: Boolean,
    isAmountError: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (ExpenseCategory) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCommentChanged: (String) -> Unit,
    onAddClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category *") },
                        isError = isCategoryError,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Text(if (expanded) "▲" else "▼")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        ExpenseCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onCategorySelected(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountChanged,
                    label = { Text("Amount *") },
                    isError = isAmountError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = commentInput,
                    onValueChange = onCommentChanged,
                    label = { Text("Comment (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAddClick) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
        title = { Text("Delete expense") },
        text = { Text("Are you sure you want to delete this expense?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = Color(0xFFC62828))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
