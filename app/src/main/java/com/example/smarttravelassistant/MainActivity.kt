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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.smarttravelassistant.model.DatabaseProvider
import com.example.smarttravelassistant.model.TravelItem
import com.example.smarttravelassistant.model.TravelRepository
import kotlinx.coroutines.launch
import com.example.smarttravelassistant.ui.theme.ExpensesScreen
import com.example.smarttravelassistant.model.ExpenseRepository
import com.example.smarttravelassistant.ui.theme.ItineraryScreen


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val requestPostNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DatabaseProvider.init(applicationContext)
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
            val expenseRepository = remember { ExpenseRepository() }
            val repository = remember { TravelRepository() }
            var refreshToken by remember { mutableLongStateOf(0L) }
            var menuExpanded by remember { mutableStateOf(false) }

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
                                                    repository.clearAll()
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
                                    repository.add(
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
                        repository = repository,
                        refreshToken = refreshToken
                    )
                    1 -> ExpensesScreen(
                        padding = padding,
                        repository = expenseRepository
                    )
                    2 -> ReminderScreen(padding) {
                        showNotification(context, "Reminder ${System.currentTimeMillis()}")
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

private fun showNotification(context: Context, message: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    ) {
        val builder = NotificationCompat.Builder(context, "travel_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Smart Travel Assistant")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}




