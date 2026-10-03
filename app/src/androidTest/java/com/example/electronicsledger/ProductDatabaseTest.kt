package com.example.electronicsledger

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ProductDatabaseTest {
    @Test fun recordsSurviveReopenAndSupportEditDeleteAndRollback() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("electronics.db")
        var database = ProductDatabase(context)
        try {
            database.save(Product(name = "测试手机", priceCents = 600000, category = Category.PHONE, purchasedOn = LocalDate.of(2026, 10, 1)))
            database.close()
            database = ProductDatabase(context)
            val saved = database.all().single()
            assertEquals("测试手机", saved.name)
            assertEquals(600000L, saved.priceCents)
            database.save(saved.copy(name = "新名称", priceCents = 500000))
            assertEquals("新名称", database.all().single().name)
            try {
                database.changeAndRead { database.delete(saved.id); error("Simulated failure") }
                fail("Transaction must fail")
            } catch (_: IllegalStateException) { }
            assertEquals(1, database.all().size)
            database.delete(saved.id)
            assertTrue(database.all().isEmpty())
        } finally {
            database.close()
            context.deleteDatabase("electronics.db")
        }
    }
}
