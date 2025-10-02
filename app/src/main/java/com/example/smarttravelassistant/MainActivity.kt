package com.example.smarttravelassistant

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest.permission.POST_NOTIFICATIONS

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
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Itinerary", "Expenses", "Reminders")

            Scaffold(
                topBar = {
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
            ) { padding ->
                when (selectedTab) {
                    0 -> ItineraryScreen(padding)
                    1 -> ExpensesScreen(padding)
                    2 -> ReminderScreen(padding) {
                        showNotification("Reminder ${System.currentTimeMillis()}")
                    }
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

    private fun showNotification(message: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val builder = NotificationCompat.Builder(this, "travel_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Smart Travel Assistant")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }
}

@Composable
fun ItineraryScreen(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        items(listOf("Flight to Paris", "Hotel Check-in", "Dinner at Eiffel Tower")) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation()
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun ExpensesScreen(padding: PaddingValues) {
    var expense by remember { mutableStateOf("") }
    val expenses = remember { mutableStateListOf<Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = expense,
            onValueChange = { expense = it },
            label = { Text("Enter expense amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                expense.toIntOrNull()?.let { expenses.add(it) }
                expense = ""
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total: $${expenses.sum()}", style = MaterialTheme.typography.titleLarge)

        LazyColumn {
            items(expenses) { e ->
                Text("Expense: $${e}", modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
fun ReminderScreen(padding: PaddingValues, onSetReminder: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Set a reminder for your trip!")
        Button(
            onClick = onSetReminder,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Send Reminder Notification")
        }
    }
}



