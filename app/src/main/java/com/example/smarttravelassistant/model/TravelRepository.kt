package com.example.smarttravelassistant.model

class TravelRepository(
    private val dao: TravelDao = DatabaseProvider.db.travelDao()
) {
    suspend fun getAll(): List<TravelItem> = dao.getAllItems()
    suspend fun add(item: TravelItem) = dao.insertItem(item)
    suspend fun remove(item: TravelItem) = dao.deleteItem(item)
    suspend fun clearAll() = dao.deleteAll()
}




