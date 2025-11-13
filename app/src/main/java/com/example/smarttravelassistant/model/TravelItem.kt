package com.example.smarttravelassistant.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_items")
data class TravelItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val cost: Double
)
