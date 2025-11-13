package com.example.smarttravelassistant.ui.theme

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smarttravelassistant.model.TravelItem
import com.example.smarttravelassistant.model.TravelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun ItineraryScreen(
    padding: PaddingValues,
    repository: TravelRepository,
    refreshToken: Long
) {
    val scope = rememberCoroutineScope()
    var travelItems by remember { mutableStateOf<List<TravelItem>>(emptyList()) }

    var weatherText by remember { mutableStateOf("Loading weather…") }
    var weatherError by remember { mutableStateOf<String?>(null) }

    var showAddEdit by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TravelItem?>(null) }
    var confirmDelete by remember { mutableStateOf<TravelItem?>(null) }

    suspend fun loadItins() { travelItems = repository.getAll() }
    suspend fun loadWeather() {
        weatherError = null
        try {
            val s = withContext(Dispatchers.IO) {
                URL("https://api.open-meteo.com/v1/forecast?latitude=1.29&longitude=103.85&current_weather=true").readText()
            }
            val cw = JSONObject(s).getJSONObject("current_weather")
            val t = cw.getDouble("temperature")
            val w = cw.getDouble("windspeed")
            val code = cw.optInt("weathercode", -1)
            weatherText = "Singapore • ${t}°C • Wind ${w} km/h • code $code"
        } catch (e: Exception) {
            weatherText = "Weather unavailable"
            weatherError = e.localizedMessage
        }
    }

    LaunchedEffect(Unit) {
        loadItins()
        loadWeather()
    }
    LaunchedEffect(refreshToken) {
        loadItins()
        loadWeather()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Current Weather", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(weatherText, style = MaterialTheme.typography.bodyMedium)
                    if (weatherError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(weatherError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                OutlinedButton(onClick = { scope.launch { loadWeather() } }) { Text("Refresh") }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { editing = null; showAddEdit = true }) { Text("Add item") }
            OutlinedButton(onClick = { scope.launch { loadItins() } }) { Text("Refresh") }
            OutlinedButton(onClick = {
                scope.launch {
                    repository.clearAll()
                    travelItems = emptyList()
                }
            }) { Text("Clear all") }
        }

        Spacer(Modifier.height(12.dp))

        if (travelItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No itineraries yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(travelItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = item; showAddEdit = true },
                        elevation = CardDefaults.cardElevation()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("Date: ${item.date}", style = MaterialTheme.typography.bodyMedium)
                                Text("Cost: $${item.cost}", style = MaterialTheme.typography.bodyMedium)
                                if (item.description.isNotBlank()) {
                                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            var menu by remember { mutableStateOf(false) }
                            IconButton(onClick = { menu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        menu = false
                                        editing = item
                                        showAddEdit = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menu = false
                                        confirmDelete = item
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEdit) {
        AddEditItineraryDialog(
            initial = editing,
            onDismiss = { showAddEdit = false },
            onSave = { title, desc, dateStr, cost ->
                val target = editing
                val toSave = if (target == null) {
                    TravelItem(title = title, description = desc, date = dateStr, cost = cost)
                } else {
                    target.copy(title = title, description = desc, date = dateStr, cost = cost)
                }
                scope.launch {
                    repository.add(toSave)
                    loadItins()
                    showAddEdit = false
                }
            }
        )
    }

    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete item") },
            text = { Text("Delete “${item.title}”?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = null
                    scope.launch {
                        repository.remove(item)
                        loadItins()
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddEditItineraryDialog(
    initial: TravelItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, dateStr: String, cost: Double) -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    fun dateToString(d: LocalDate): String = d.format(formatter)
    fun stringToDate(s: String): LocalDate? = runCatching { LocalDate.parse(s, formatter) }.getOrNull()

    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var desc by remember { mutableStateOf(initial?.description.orEmpty()) }
    var costText by remember { mutableStateOf(if (initial != null) initial.cost.toString() else "") }
    var dateStr by remember { mutableStateOf(initial?.date.takeIf { !it.isNullOrBlank() } ?: dateToString(LocalDate.now())) }

    fun openDatePicker() {
        val cal = Calendar.getInstance().apply {
            val d = stringToDate(dateStr) ?: LocalDate.now()
            set(Calendar.YEAR, d.year)
            set(Calendar.MONTH, d.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, d.dayOfMonth)
        }
        DatePickerDialog(
            context,
            { _, y, m, day ->
                val picked = LocalDate.of(y, m + 1, day)
                dateStr = dateToString(picked)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add itinerary item" else "Edit itinerary item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = dateStr, onValueChange = {}, label = { Text("Date") }, readOnly = true, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { openDatePicker() }) { Text("Pick") }
                }
                OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Cost") }, singleLine = true)
            }
        },
        confirmButton = {
            val costOk = costText.toDoubleOrNull() != null
            val canSave = title.isNotBlank() && costOk
            TextButton(enabled = canSave, onClick = {
                onSave(title.trim(), desc.trim(), dateStr, costText.toDoubleOrNull() ?: 0.0)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}












