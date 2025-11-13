package com.example.smarttravelassistant

import androidx.lifecycle.ViewModel
import org.junit.Assert.assertEquals
import org.junit.Test


class BudgetViewModel : ViewModel() {

    var total: Double = 0.0
        private set

    var count: Int = 0
        private set

    fun addExpense(amount: Double) {
        if (amount <= 0.0) return
        total += amount
        count += 1
    }

    fun reset() {
        total = 0.0
        count = 0
    }
}

class SimpleViewModelTest {

    @Test
    fun addExpense_updatesTotalAndCount() {
        val vm = BudgetViewModel()


        assertEquals(0.0, vm.total, 0.0001)
        assertEquals(0, vm.count)


        vm.addExpense(50.0)
        assertEquals(50.0, vm.total, 0.0001)
        assertEquals(1, vm.count)


        vm.addExpense(25.5)
        assertEquals(75.5, vm.total, 0.0001)
        assertEquals(2, vm.count)


        vm.addExpense(0.0)
        vm.addExpense(-10.0)
        assertEquals(75.5, vm.total, 0.0001)
        assertEquals(2, vm.count)
    }

    @Test
    fun reset_clearsState() {
        val vm = BudgetViewModel()

        vm.addExpense(100.0)
        vm.addExpense(50.0)
        assertEquals(150.0, vm.total, 0.0001)
        assertEquals(2, vm.count)

        vm.reset()

        assertEquals(0.0, vm.total, 0.0001)
        assertEquals(0, vm.count)
    }
}


