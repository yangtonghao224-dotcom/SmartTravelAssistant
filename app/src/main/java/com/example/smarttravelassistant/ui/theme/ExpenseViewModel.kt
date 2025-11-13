package com.example.smarttravelassistant.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravelassistant.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repo: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _items = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val items: StateFlow<List<ExpenseItem>> = _items

    private val _total = MutableStateFlow(0.0)
    val total: StateFlow<Double> = _total

    private val _categoryTotals = MutableStateFlow<List<CategoryTotal>>(emptyList())
    val categoryTotals: StateFlow<List<CategoryTotal>> = _categoryTotals

    fun refresh() {
        viewModelScope.launch {
            _items.value = repo.getAll()
            _total.value = repo.total()
            _categoryTotals.value = repo.sumByCategory()
        }
    }

    fun add(title: String, amount: Double, category: String, date: String) {
        viewModelScope.launch {
            repo.add(ExpenseItem(title = title, amount = amount, category = category, date = date))
            refresh()
        }
    }

    fun delete(item: ExpenseItem) {
        viewModelScope.launch {
            repo.remove(item)
            refresh()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.clearAll()
            refresh()
        }
    }
}


