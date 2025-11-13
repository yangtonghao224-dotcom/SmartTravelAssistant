package com.example.smarttravelassistant.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarttravelassistant.model.ExpenseItem

@Composable
fun ExpensesScreen(
    padding: PaddingValues,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val items = viewModel.items
    val total = viewModel.total

    val title = viewModel.title
    val amount = viewModel.amount
    val category = viewModel.category
    val date = viewModel.date

    val editing = viewModel.editing
    val showEdit = viewModel.showEditDialog
    val confirmDelete = viewModel.confirmDelete

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Expenses", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Total: $${"%.2f".format(total)}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.refresh() }) { Text("Refresh") }
                    OutlinedButton(onClick = { viewModel.clearAll() }) { Text("Clear all") }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Add / Edit Expense", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { viewModel.title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { viewModel.amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = category,
                onValueChange = { viewModel.category = it },
                label = { Text("Category") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = date,
                onValueChange = { viewModel.date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(Modifier.height(10.dp))

        Button(onClick = { viewModel.onAddOrEdit() }) {
            Text(if (editing == null) "Add Expense" else "Save Changes")
        }

        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No expenses yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { item ->
                    ExpenseCard(
                        item = item,
                        onEdit = { viewModel.startEdit(item) },
                        onDelete = { viewModel.requestDelete(item) }
                    )
                }
            }
        }
    }

    if (showEdit) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelEdit() },
            title = { Text("Edit Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { viewModel.title = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(value = amount, onValueChange = { viewModel.amount = it }, label = { Text("Amount") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { viewModel.category = it }, label = { Text("Category") }, singleLine = true)
                    OutlinedTextField(value = date, onValueChange = { viewModel.date = it }, label = { Text("Date") }, singleLine = true)
                }
            },
            confirmButton = {
                val ok = amount.toDoubleOrNull() != null && title.isNotBlank()
                TextButton(enabled = ok, onClick = { viewModel.onAddOrEdit() }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelEdit() }) { Text("Cancel") } }
        )
    }

    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.confirmDelete = null },
            title = { Text("Delete Expense") },
            text = { Text("Delete \"${item.title}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.performDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { viewModel.confirmDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ExpenseCard(
    item: ExpenseItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Amount: $${"%.2f".format(item.amount)}")
                Text("Category: ${item.category}")
                Text("Date: ${item.date}")
            }

            var menu by remember { mutableStateOf(false) }

            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit() })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
            }
        }
    }
}


