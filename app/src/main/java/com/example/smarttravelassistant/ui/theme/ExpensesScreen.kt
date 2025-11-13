package com.example.smarttravelassistant.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.smarttravelassistant.model.ExpenseItem
import com.example.smarttravelassistant.model.ExpenseRepository

@Composable
fun ExpensesScreen(
    padding: PaddingValues,
    repository: ExpenseRepository
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var date by remember { mutableStateOf("") }

    var list by remember { mutableStateOf<List<ExpenseItem>>(emptyList()) }
    var total by remember { mutableStateOf(0.0) }

    fun refresh() {
        scope.launch {
            list = repository.getAll()
            total = repository.total()
        }
    }

    LaunchedEffect(Unit) { refresh() }

    fun addExpense() {
        val amt = amountText.toDoubleOrNull() ?: 0.0
        if (title.isBlank() || amt <= 0.0 || date.isBlank()) return
        scope.launch {
            repository.add(
                ExpenseItem(
                    title = title.trim(),
                    amount = amt,
                    category = category,
                    date = date.trim()
                )
            )
            title = ""
            amountText = ""
            date = ""
            refresh()
        }
    }

    fun clearAll() {
        scope.launch {
            repository.clearAll()
            refresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Expenses", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Title (e.g. Dinner)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountText, onValueChange = { amountText = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = category, onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = date, onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { addExpense() }) { Text("Add Expense") }
            OutlinedButton(onClick = { refresh() }) { Text("Refresh") }
            OutlinedButton(onClick = { clearAll() }) { Text("Clear all") }
        }

        Text("Total: $${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No expenses yet. Add one above.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { e ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(e.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Amount: $${"%.2f".format(e.amount)}")
                            Text("Category: ${e.category}")
                            Text("Date: ${e.date}")
                        }
                    }
                }
            }
        }
    }
}
