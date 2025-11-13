package com.example.smarttravelassistant.model

import androidx.room.*

@Dao
interface TravelDao {
    @Query("SELECT * FROM travel_items")
    suspend fun getAllItems(): List<TravelItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TravelItem)

    @Delete
    suspend fun deleteItem(item: TravelItem)

    @Query("DELETE FROM travel_items")
    suspend fun deleteAll()
}




