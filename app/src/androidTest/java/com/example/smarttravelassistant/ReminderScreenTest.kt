package com.example.smarttravelassistant

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class ReminderScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reminderScreen_showsExchangeRateSection() {
        composeRule.setContent {
            ReminderScreen(
                padding = PaddingValues(0.dp),
                tripName = "My Trip",
                budget = 1000.0,
                notify50 = true,
                notify70 = true,
                notify90 = true,
                notify100 = true,
                onTripNameChange = {},
                onBudgetChange = {},
                onNotify50Change = {},
                onNotify70Change = {},
                onNotify90Change = {},
                onNotify100Change = {},
                onTestNotification = {}
            )
        }

        composeRule.onNodeWithText("Exchange Rate").assertIsDisplayed()
        composeRule.onNodeWithText("Send test notification").assertIsDisplayed()
    }
}
