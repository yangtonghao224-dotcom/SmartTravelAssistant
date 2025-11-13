package com.example.smarttravelassistant.model

class ExpenseRepository(
    private val dao: ExpenseDao = DatabaseProvider.db.expenseDao()
) {

    // 获取所有支出
    suspend fun getAll(): List<ExpenseItem> = dao.getAll()

    // 添加支出
    suspend fun add(item: ExpenseItem) = dao.insert(item)

    // 删除单条支出
    suspend fun remove(item: ExpenseItem) = dao.delete(item)

    // 清空所有支出
    suspend fun clearAll() = dao.deleteAll()

    // 获取总支出金额
    suspend fun total(): Double = dao.totalAmount()

    // 获取按分类统计的支出总额
    suspend fun sumByCategory(): List<CategoryTotal> = dao.sumByCategory()
}


