package com.example.smarttravelassistant.model

import android.content.Context
import androidx.room.Room


object DatabaseProvider {
    @Volatile
    private var _db: TravelDatabase? = null
    val db: TravelDatabase
        get() = _db ?: error("DatabaseProvider not initialized. Call init(context) first.")

    fun init(context: Context) {
        if (_db == null) {
            synchronized(this) {
                if (_db == null) {
                    _db = Room.databaseBuilder(
                        context.applicationContext,
                        TravelDatabase::class.java,
                        "travel.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
    }
}
