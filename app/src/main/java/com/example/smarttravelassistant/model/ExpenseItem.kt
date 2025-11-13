package com.example.smarttravelassistant.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_items")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String
)

