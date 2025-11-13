package com.example.smarttravelassistant.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense_items ORDER BY id DESC")
    suspend fun getAll(): List<ExpenseItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ExpenseItem)

    @Delete
    suspend fun delete(item: ExpenseItem)

    @Query("DELETE FROM expense_items")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense_items")
    suspend fun totalAmount(): Double

    @Query(
        """
        SELECT 
            category AS category,
            COALESCE(SUM(amount), 0) AS total
        FROM expense_items
        GROUP BY category
        ORDER BY total DESC
        """
    )
    suspend fun sumByCategory(): List<CategoryTotal>
}




