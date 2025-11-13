package com.example.smarttravelassistant.model

class ExpenseRepository(
    private val dao: ExpenseDao = DatabaseProvider.db.expenseDao()
) {
    suspend fun getAll(): List<ExpenseItem> = dao.getAll()

    suspend fun add(item: ExpenseItem) = dao.insert(item)

    suspend fun remove(item: ExpenseItem) = dao.delete(item)

    suspend fun clearAll() = dao.deleteAll()

    suspend fun total(): Double = dao.totalAmount()

    suspend fun sumByCategory(): List<CategoryTotal> = dao.sumByCategory()
}



