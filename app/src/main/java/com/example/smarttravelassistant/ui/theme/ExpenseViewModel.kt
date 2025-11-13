package com.example.smarttravelassistant.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttravelassistant.model.ExpenseItem
import com.example.smarttravelassistant.model.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repo: ExpenseRepository
) : ViewModel() {


    var items by mutableStateOf<List<ExpenseItem>>(emptyList())
        private set

    var total by mutableStateOf(0.0)
        private set


    var title by mutableStateOf("")
    var amount by mutableStateOf("")
    var category by mutableStateOf("")
    var date by mutableStateOf("")


    var editing: ExpenseItem? by mutableStateOf(null)
    var showEditDialog by mutableStateOf(false)
    var confirmDelete: ExpenseItem? by mutableStateOf(null)

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        items = repo.getAll()
        total = repo.total()
    }

    fun onAddOrEdit() {
        val a = amount.toDoubleOrNull() ?: return
        val base = editing

        val toSave = if (base == null) {
            ExpenseItem(
                title = title.trim(),
                amount = a,
                category = category.trim(),
                date = date.trim()
            )
        } else {
            base.copy(
                title = title.trim(),
                amount = a,
                category = category.trim(),
                date = date.trim()
            )
        }

        viewModelScope.launch {
            repo.add(toSave)
            clearForm()
            editing = null
            showEditDialog = false
            refresh()
        }
    }

    fun startEdit(item: ExpenseItem) {
        editing = item
        title = item.title
        amount = item.amount.toString()
        category = item.category
        date = item.date
        showEditDialog = true
    }

    fun cancelEdit() {
        showEditDialog = false
        editing = null
        clearForm()
    }

    fun requestDelete(item: ExpenseItem) {
        confirmDelete = item
    }

    fun performDelete() {
        val target = confirmDelete ?: return
        viewModelScope.launch {
            repo.remove(target)
            confirmDelete = null
            refresh()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.clearAll()
            items = emptyList()
            total = 0.0
        }
    }

    private fun clearForm() {
        title = ""
        amount = ""
        category = ""
        date = ""
    }
}



