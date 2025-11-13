package com.example.smarttravelassistant

import com.example.smarttravelassistant.model.CategoryTotal
import org.junit.Assert.assertEquals
import org.junit.Test


class BudgetUtilTest {



    private fun calcPercent(totalForCategory: Double, grandTotal: Double): Double {
        return if (grandTotal > 0.0) {
            totalForCategory / grandTotal
        } else {
            0.0
        }
    }

    @Test
    fun categoryPercent_foodIs50Percent() {

        val food = CategoryTotal(category = "Food", total = 150.0)
        val grandTotal = 300.0

        val percent = calcPercent(food.total, grandTotal)


        assertEquals(0.5, percent, 0.0001)
    }

    @Test
    fun categoryPercent_isZeroWhenGrandTotalIsZero() {

        val food = CategoryTotal(category = "Food", total = 150.0)
        val grandTotal = 0.0

        val percent = calcPercent(food.total, grandTotal)


        assertEquals(0.0, percent, 0.0001)
    }

    @Test
    fun categoryPercent_multipleCategoriesSumToOne() {

        val food = CategoryTotal(category = "Food", total = 100.0)
        val transport = CategoryTotal(category = "Transport", total = 200.0)
        val hotel = CategoryTotal(category = "Hotel", total = 700.0)
        val grandTotal = 1000.0

        val pFood = calcPercent(food.total, grandTotal)
        val pTransport = calcPercent(transport.total, grandTotal)
        val pHotel = calcPercent(hotel.total, grandTotal)

        val sum = pFood + pTransport + pHotel


        assertEquals(1.0, sum, 0.0001)
    }
}

