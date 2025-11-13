package com.example.smarttravelassistant

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.smarttravelassistant.model.ExpenseRepository
import com.example.smarttravelassistant.model.TravelItem
import com.example.smarttravelassistant.model.TravelRepository
import com.example.smarttravelassistant.network.ExchangeRateService
import com.example.smarttravelassistant.ui.theme.ExpensesScreen
import com.example.smarttravelassistant.ui.theme.ItineraryScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Locale

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val requestPostNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPostNotificationsPermission.launch(POST_NOTIFICATIONS)
        }

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Itinerary", "Expenses", "Reminders")

            val travelRepo = remember { TravelRepository() }
            val expenseRepo = remember { ExpenseRepository() }

            var refreshToken by remember { mutableLongStateOf(0L) }
            var menuExpanded by remember { mutableStateOf(false) }

            var tripName by remember { mutableStateOf("My Trip") }
            var budget by remember { mutableStateOf(0.0) }

            var notify50 by remember { mutableStateOf(true) }
            var notify70 by remember { mutableStateOf(true) }
            var notify90 by remember { mutableStateOf(true) }
            var notify100 by remember { mutableStateOf(true) }

            var lastAlertLevel by remember { mutableIntStateOf(0) }

            Scaffold(
                topBar = {
                    Column {
                        CenterAlignedTopAppBar(
                            title = { Text(tabs[selectedTab]) },
                            actions = {
                                if (selectedTab == 0) {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Refresh") },
                                            onClick = {
                                                menuExpanded = false
                                                refreshToken++
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Clear all") },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    travelRepo.clearAll()
                                                    refreshToken++
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        )
                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    text = { Text(title) },
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index }
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    if (selectedTab == 0) {
                        FloatingActionButton(
                            onClick = {
                                scope.launch {
                                    travelRepo.add(
                                        TravelItem(
                                            title = "Flight to Paris",
                                            description = "SQ333 20:15",
                                            date = "2025-11-20",
                                            cost = 850.0
                                        )
                                    )
                                    refreshToken++
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            ) { padding ->
                when (selectedTab) {
                    0 -> ItineraryScreen(
                        padding = padding,
                        repository = travelRepo,
                        refreshToken = refreshToken
                    )

                    1 -> ExpensesScreen(
                        padding = padding,
                        budget = budget,
                        tripName = tripName,
                        notify50 = notify50,
                        notify70 = notify70,
                        notify90 = notify90,
                        notify100 = notify100,
                        lastAlertLevel = lastAlertLevel,
                        onAlertLevelChanged = { level -> lastAlertLevel = level },
                        onBudgetAlert = { level, total, maxBudget ->
                            val titleText = if (tripName.isBlank()) {
                                "Budget Alert"
                            } else {
                                "Budget Alert – $tripName"
                            }
                            val msg = "Spent ${"%.2f".format(total)} / " +
                                    "${"%.2f".format(maxBudget)} (${level}%)"
                            showNotification(context, "$titleText: $msg")
                        }
                    )

                    2 -> ReminderScreen(
                        padding = padding,
                        tripName = tripName,
                        budget = budget,
                        notify50 = notify50,
                        notify70 = notify70,
                        notify90 = notify90,
                        notify100 = notify100,
                        onTripNameChange = { tripName = it },
                        onBudgetChange = { budget = it },
                        onNotify50Change = { notify50 = it },
                        onNotify70Change = { notify70 = it },
                        onNotify90Change = { notify90 = it },
                        onNotify100Change = { notify100 = it },
                        onTestNotification = {
                            showNotification(context, "Test reminder for $tripName")
                        }
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "travel_channel",
                "Travel Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun ReminderScreen(
    padding: PaddingValues,
    tripName: String,
    budget: Double,
    notify50: Boolean,
    notify70: Boolean,
    notify90: Boolean,
    notify100: Boolean,
    onTripNameChange: (String) -> Unit,
    onBudgetChange: (Double) -> Unit,
    onNotify50Change: (Boolean) -> Unit,
    onNotify70Change: (Boolean) -> Unit,
    onNotify90Change: (Boolean) -> Unit,
    onNotify100Change: (Boolean) -> Unit,
    onTestNotification: () -> Unit
) {
    var budgetText by remember { mutableStateOf(if (budget > 0) budget.toString() else "") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExchangeRateCard()

        Spacer(Modifier.height(8.dp))

        Text("Trip & Budget", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = tripName,
            onValueChange = onTripNameChange,
            label = { Text("Trip name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = budgetText,
            onValueChange = {
                budgetText = it
                val v = it.toDoubleOrNull()
                if (v != null && v >= 0.0) {
                    onBudgetChange(v)
                }
            },
            label = { Text("Total budget") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Text("When to notify:", style = MaterialTheme.typography.titleMedium)

        ReminderSwitchRow(
            label = "50% of budget",
            checked = notify50,
            onCheckedChange = onNotify50Change
        )
        ReminderSwitchRow(
            label = "70% of budget",
            checked = notify70,
            onCheckedChange = onNotify70Change
        )
        ReminderSwitchRow(
            label = "90% of budget",
            checked = notify90,
            onCheckedChange = onNotify90Change
        )
        ReminderSwitchRow(
            label = "100% or above",
            checked = notify100,
            onCheckedChange = onNotify100Change
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onTestNotification) {
            Text("Send test notification")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ReminderSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


private sealed class ExchangeUiState {
    object Loading : ExchangeUiState()
    data class Success(val rate: Double, val fromCache: Boolean) : ExchangeUiState()
    data class Error(val message: String) : ExchangeUiState()
}

@Composable
private fun ExchangeRateCard() {
    val context = LocalContext.current

    var base by remember { mutableStateOf("SGD") }
    var target by remember { mutableStateOf("CNY") }
    var state by remember { mutableStateOf<ExchangeUiState>(ExchangeUiState.Loading) }
    var expanded by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var amountText by remember { mutableStateOf("1") }

    val currencyList = listOf(
        "SGD", "CNY", "USD", "EUR", "JPY", "GBP", "AUD", "MYR", "THB", "KRW", "TWD"
    )

    LaunchedEffect(base, target, reloadKey) {
        state = ExchangeUiState.Loading
        try {
            val response = ExchangeRateService.api.getLatest(base, target)
            val ratePerUnit = response.rates?.get(target)

            if (ratePerUnit != null) {
                cacheRate(context, base, target, ratePerUnit)
                state = ExchangeUiState.Success(ratePerUnit, fromCache = false)
            } else {
                val cached = getCachedRate(context, base, target)
                if (cached != null) {
                    state = ExchangeUiState.Success(cached, fromCache = true)
                } else {
                    state = ExchangeUiState.Error("Rate not found")
                }
            }
        } catch (e: IOException) {

            val cached = getCachedRate(context, base, target)
            state = if (cached != null) {
                ExchangeUiState.Success(cached, fromCache = true)
            } else {
                ExchangeUiState.Error("No internet connection and no cached rate.")
            }
        } catch (e: HttpException) {
            state = ExchangeUiState.Error("Server error (${e.code()}). Please try again later.")
        } catch (e: Exception) {
            state = ExchangeUiState.Error("Unexpected error: ${e.localizedMessage}")
        }
    }

    val baseSymbol = currencySymbol(base)
    val targetSymbol = currencySymbol(target)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Exchange Rate", style = MaterialTheme.typography.titleMedium)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Base: $base ($baseSymbol)")

                OutlinedButton(
                    onClick = {
                        val oldBase = base
                        base = target
                        target = oldBase
                        reloadKey++
                    }
                ) {
                    Text("Swap")
                }

                Box {
                    Button(onClick = { expanded = true }) {
                        Text("To: $target ($targetSymbol)")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        currencyList.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    target = code
                                    expanded = false
                                    reloadKey++
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { newText ->
                    val filtered = newText.filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        amountText = filtered
                    }
                },
                label = { Text("Amount ($baseSymbol)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            when (val s = state) {
                ExchangeUiState.Loading -> {
                    Text("Loading…")
                }

                is ExchangeUiState.Success -> {
                    val ratePerUnit = s.rate
                    val amount = amountText.toDoubleOrNull() ?: 1.0
                    val converted = ratePerUnit * amount

                    val formattedUnit = String.format(Locale.getDefault(), "%.4f", ratePerUnit)
                    val formattedConverted =
                        String.format(Locale.getDefault(), "%.2f", converted)

                    Text("1 $base ($baseSymbol) ≈ $formattedUnit $target ($targetSymbol)")

                    if (amount != 1.0) {
                        Text(
                            "$amount $base ($baseSymbol) ≈ " +
                                    "$formattedConverted $target ($targetSymbol)"
                        )
                    }

                    if (s.fromCache) {
                        Text(
                            text = "Offline: showing last saved rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                is ExchangeUiState.Error -> {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedButton(onClick = { reloadKey++ }) {
                Text("Refresh")
            }
        }
    }
}


private fun cacheRate(context: Context, base: String, target: String, rate: Double) {
    val prefs = context.getSharedPreferences("exchange_cache", Context.MODE_PRIVATE)
    val key = "${base}_$target"
    prefs.edit()
        .putString("${key}_value", rate.toString())
        .putLong("${key}_time", System.currentTimeMillis())
        .apply()
}


private fun getCachedRate(context: Context, base: String, target: String): Double? {
    val prefs = context.getSharedPreferences("exchange_cache", Context.MODE_PRIVATE)
    val key = "${base}_$target"
    val value = prefs.getString("${key}_value", null)
    return value?.toDoubleOrNull()
}

fun currencySymbol(code: String): String = when (code) {
    "SGD", "USD", "AUD" -> "$"
    "CNY", "JPY" -> "¥"
    "EUR" -> "€"
    "GBP" -> "£"
    "MYR" -> "RM"
    "THB" -> "฿"
    "KRW" -> "₩"
    "TWD" -> "NT$"
    else -> code
}

private fun showNotification(context: Context, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val builder = NotificationCompat.Builder(context, "travel_channel")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Smart Travel Assistant")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
