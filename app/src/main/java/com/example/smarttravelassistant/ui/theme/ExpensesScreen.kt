package com.example.smarttravelassistant.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarttravelassistant.model.CategoryTotal
import com.example.smarttravelassistant.model.ExpenseItem
import java.util.Locale
import kotlin.math.min

@Composable
fun ExpensesScreen(
    padding: PaddingValues,
    budget: Double,
    tripName: String,
    notify50: Boolean,
    notify70: Boolean,
    notify90: Boolean,
    notify100: Boolean,
    lastAlertLevel: Int,
    onAlertLevelChanged: (Int) -> Unit,
    onBudgetAlert: (Int, Double, Double) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val items = viewModel.items
    val total = viewModel.total
    val categoryTotals = viewModel.categoryTotals

    val title = viewModel.title
    val amount = viewModel.amount
    val category = viewModel.category
    val date = viewModel.date

    val editing = viewModel.editing
    val showEdit = viewModel.showEditDialog
    val confirmDelete = viewModel.confirmDelete

    // 🌟 根据预算发通知
    LaunchedEffect(total, budget, notify50, notify70, notify90, notify100) {
        if (budget <= 0.0) return@LaunchedEffect
        val percent = total / budget
        var target = 0

        fun check(level: Int, enabled: Boolean) {
            if (enabled && percent >= level / 100.0 && level > target) {
                target = level
            }
        }

        check(50, notify50)
        check(70, notify70)
        check(90, notify90)
        check(100, notify100)

        if (target > 0 && target > lastAlertLevel) {
            onBudgetAlert(target, total, budget)
            onAlertLevelChanged(target)
        }
    }

    // 支持下滑
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // 顶部 Summary
        item {
            SummaryCard(
                budget = budget,
                total = total,
                onRefresh = { viewModel.refresh() },
                onClearAll = { viewModel.clearAll() }
            )
        }

        // 分类饼图
        if (categoryTotals.isNotEmpty()) {
            item {
                CategorySummaryCard(categoryTotals, total)
            }
        }

        // 输入框
        item {
            AddEditSection(
                title = title,
                amount = amount,
                category = category,
                date = date,
                isEditing = editing != null,
                onTitle = { viewModel.title = it },
                onAmount = { viewModel.amount = it },
                onCategory = { viewModel.category = it },
                onDate = { viewModel.date = it },
                onSubmit = { viewModel.onAddOrEdit() }
            )
        }

        // 列表
        if (items.isEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses yet.")
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                ExpenseCard(
                    item = item,
                    onEdit = { viewModel.startEdit(item) },
                    onDelete = { viewModel.requestDelete(item) }
                )
            }
        }
    }

    // 编辑弹窗
    if (showEdit) {
        EditDialog(viewModel)
    }

    // 删除弹窗
    confirmDelete?.let {
        AlertDialog(
            onDismissRequest = { viewModel.confirmDelete = null },
            title = { Text("Delete Expense") },
            text = { Text("Delete \"${it.title}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.performDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { viewModel.confirmDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SummaryCard(
    budget: Double,
    total: Double,
    onRefresh: () -> Unit,
    onClearAll: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
        Column(Modifier.padding(14.dp)) {
            Text("My Trip", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Total: $${"%.2f".format(total)}")

            if (budget > 0) {
                val percent = total / budget
                Text("Budget: $${"%.2f".format(budget)} (${String.format("%.1f", percent * 100)}%)")

                LinearProgressIndicator(
                    progress = percent.coerceIn(0.0, 1.0).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(6.dp)
                )
            } else {
                Text("No budget set")
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRefresh) { Text("Refresh") }
                OutlinedButton(onClick = onClearAll) { Text("Clear all") }
            }
        }
    }
}

@Composable
private fun CategorySummaryCard(
    data: List<CategoryTotal>,
    grandTotal: Double
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
        Column(Modifier.padding(14.dp)) {
            Text("By Category", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            val size = 180.dp

            CategoryPieChart(
                segments = data.map {
                    CategorySegment(
                        name = it.category.ifBlank { "Uncategorised" },
                        total = it.total,
                        color = randomColor(it.category)
                    )
                },
                grandTotal = grandTotal,
                modifier = Modifier.size(size)
            )

            Spacer(Modifier.height(8.dp))

            data.forEach {
                CategoryRow(it, grandTotal)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

data class CategorySegment(
    val name: String,
    val total: Double,
    val color: Color
)

@Composable
private fun CategoryPieChart(
    segments: List<CategorySegment>,
    grandTotal: Double,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty() || grandTotal <= 0) return

    Canvas(modifier = modifier) {
        var start = -90f

        segments.forEach { seg ->
            val percent = (seg.total / grandTotal).toFloat().coerceIn(0f, 1f)
            val sweep = percent * 360f

            drawArc(
                color = seg.color,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true
            )

            start += sweep
        }
    }
}

@Composable
private fun AddEditSection(
    title: String,
    amount: String,
    category: String,
    date: String,
    isEditing: Boolean,
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
    onCategory: (String) -> Unit,
    onDate: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Text("Add / Edit Expense", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))

    OutlinedTextField(value = title, onValueChange = onTitle, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))

    OutlinedTextField(value = amount, onValueChange = onAmount, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(6.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = category,
            onValueChange = onCategory,
            label = { Text("Category") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = date,
            onValueChange = onDate,
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(10.dp))
    Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
        Text(if (isEditing) "Save Changes" else "Add Expense")
    }
}

@Composable
private fun EditDialog(viewModel: ExpenseViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.cancelEdit() },
        title = { Text("Edit Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = viewModel.title, onValueChange = { viewModel.title = it }, label = { Text("Title") })
                OutlinedTextField(value = viewModel.amount, onValueChange = { viewModel.amount = it }, label = { Text("Amount") })
                OutlinedTextField(value = viewModel.category, onValueChange = { viewModel.category = it }, label = { Text("Category") })
                OutlinedTextField(value = viewModel.date, onValueChange = { viewModel.date = it }, label = { Text("Date") })
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.onAddOrEdit() }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.cancelEdit() }) { Text("Cancel") }
        }
    )
}

@Composable
private fun ExpenseCard(
    item: ExpenseItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("Amount: $${"%.2f".format(item.amount)}")
                Text("Category: ${item.category}")
                Text("Date: ${item.date}")
            }

            var menu by remember { mutableStateOf(false) }

            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { menu = false; onEdit() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { menu = false; onDelete() }
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(ct: CategoryTotal, grandTotal: Double) {
    val percent = if (grandTotal > 0) ct.total / grandTotal else 0.0
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(ct.category.ifBlank { "Uncategorised" })
        Text(
            "${"%.2f".format(ct.total)} (${String.format(Locale.getDefault(), "%.1f", percent * 100)}%)",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun randomColor(key: String): Color {
    val hash = key.hashCode()
    return Color(
        red = (hash shr 16 and 0xFF) / 255f,
        green = (hash shr 8 and 0xFF) / 255f,
        blue = (hash and 0xFF) / 255f,
        alpha = 1f
    )
}
