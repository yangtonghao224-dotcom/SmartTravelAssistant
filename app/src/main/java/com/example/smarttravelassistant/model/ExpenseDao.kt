package com.example.smarttravelassistant.model

import androidx.room.*

@Dao
interface ExpenseDao {

    // 获取所有支出记录
    @Query("SELECT * FROM expense_items ORDER BY id DESC")
    suspend fun getAll(): List<ExpenseItem>

    // 插入或更新一条支出记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ExpenseItem)

    // 删除单条记录
    @Delete
    suspend fun delete(item: ExpenseItem)

    // 删除全部记录
    @Query("DELETE FROM expense_items")
    suspend fun deleteAll()

    // 计算总支出金额（无记录时返回 0）
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense_items")
    suspend fun totalAmount(): Double

    // 按分类统计支出总额
    @Query("""
        SELECT category AS category, 
               SUM(amount) AS total
        FROM expense_items
        GROUP BY category
        ORDER BY total DESC
    """)
    suspend fun sumByCategory(): List<CategoryTotal>
}



