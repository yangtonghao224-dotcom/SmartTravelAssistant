package com.example.smarttravelassistant.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TravelItem::class,
        ExpenseItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun travelDao(): TravelDao
    abstract fun expenseDao(): ExpenseDao
}


