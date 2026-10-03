package com.example.electronicsledger

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ProductTest {
    private fun product(date: String, cents: Long = 10000) = Product(name = "耳机", priceCents = cents, category = Category.PERIPHERAL, purchasedOn = LocalDate.parse(date))

    @Test fun purchaseDayCountsAsOne() {
        val day = LocalDate.parse("2026-10-02")
        assertEquals(1L, product("2026-10-02").daysOwned(day))
        assertEquals("100.00", product("2026-10-02").dailyCost(day).toPlainString())
    }

    @Test fun elapsedDaysIncludePurchaseDayAndRoundHalfUp() {
        assertEquals("33.33", product("2026-09-30").dailyCost(LocalDate.parse("2026-10-02")).toPlainString())
        assertEquals("0.01", product("2026-10-01", 1).dailyCost(LocalDate.parse("2026-10-02")).toPlainString())
    }

    @Test fun leapYearAndYearBoundary() {
        assertEquals(3L, product("2024-02-28").daysOwned(LocalDate.parse("2024-03-01")))
        assertEquals(2L, product("2025-12-31").daysOwned(LocalDate.parse("2026-01-01")))
    }

    @Test fun parseMoneyWithoutFloatingPointErrors() {
        assertEquals(29L, parsePriceCents("0.29"))
        assertEquals(123450L, parsePriceCents("1234.50"))
        assertEquals(0L, parsePriceCents("0"))
        assertEquals(99999999999L, parsePriceCents("999999999.99"))
        listOf("", "-1", "1.001", "NaN", "1e3", "1000000000", "1,000").forEach { assertNull(parsePriceCents(it)) }
    }

    @Test fun rejectBlankNameAndFutureDate() {
        val today = LocalDate.parse("2026-10-02")
        assertNotNull(validateProduct(" ", "10", today, today))
        assertNotNull(validateProduct("手机", "10", today.plusDays(1), today))
        assertNull(validateProduct("手机", "0.00", today, today))
    }

    @Test fun clockMovingBackDoesNotDivideByZero() {
        assertEquals("100.00", product("2026-10-02").dailyCost(LocalDate.parse("2026-10-01")).toPlainString())
    }
}
