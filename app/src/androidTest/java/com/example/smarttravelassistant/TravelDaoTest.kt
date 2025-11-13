package com.example.smarttravelassistant

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.smarttravelassistant.model.TravelDao
import com.example.smarttravelassistant.model.TravelDatabase
import com.example.smarttravelassistant.model.TravelItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelDaoTest {

    private lateinit var db: TravelDatabase
    private lateinit var dao: TravelDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        db = Room.inMemoryDatabaseBuilder(
            context,
            TravelDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.travelDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetAllItems() = runBlocking {
        val item = TravelItem(
            id = 0,
            title = "Flight to Tokyo",
            description = "JL36 18:00",
            date = "2025-12-01",
            cost = 999.0
        )

        dao.insertItem(item)

        val all = dao.getAllItems()
        assertEquals(1, all.size)
        assertEquals("Flight to Tokyo", all[0].title)
        assertEquals(999.0, all[0].cost, 0.001)
    }

    @Test
    fun deleteItem_removesFromList() = runBlocking {
        val item = TravelItem(
            id = 0,
            title = "Hotel",
            description = "3 nights",
            date = "2025-12-02",
            cost = 300.0
        )

        dao.insertItem(item)
        var all = dao.getAllItems()
        val inserted = all[0]

        dao.deleteItem(inserted)

        all = dao.getAllItems()
        assertEquals(0, all.size)
    }

    @Test
    fun deleteAll_clearsTable() = runBlocking {
        val item1 = TravelItem(
            id = 0,
            title = "Train",
            description = "Osaka -> Kyoto",
            date = "2025-12-03",
            cost = 50.0
        )
        val item2 = TravelItem(
            id = 0,
            title = "Museum",
            description = "Ticket",
            date = "2025-12-04",
            cost = 20.0
        )

        dao.insertItem(item1)
        dao.insertItem(item2)

        var all = dao.getAllItems()
        assertEquals(2, all.size)

        dao.deleteAll()

        all = dao.getAllItems()
        assertEquals(0, all.size)
    }
}


