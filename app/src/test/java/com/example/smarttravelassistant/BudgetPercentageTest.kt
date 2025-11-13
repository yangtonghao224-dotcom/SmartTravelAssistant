package com.example.smarttravelassistant

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetPercentageTest {

    private fun calculatePercent(total: Double, budget: Double): Double {
        if (budget == 0.0) return 0.0
        return (total / budget) * 100
    }

    @Test
    fun testBudgetPercent() {
        assertEquals(50.0, calculatePercent(50.0, 100.0), 0.001)
        assertEquals(75.0, calculatePercent(75.0, 100.0), 0.001)
        assertEquals(100.0, calculatePercent(200.0, 200.0), 0.001)
        assertEquals(0.0, calculatePercent(0.0, 100.0), 0.001)
        assertEquals(0.0, calculatePercent(10.0, 0.0), 0.001)
    }
}


