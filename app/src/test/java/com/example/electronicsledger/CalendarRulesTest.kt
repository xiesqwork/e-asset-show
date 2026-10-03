package com.example.electronicsledger

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarRulesTest {
    @Test fun gridSwitchesAtFourAndSevenProducts() {
        assertEquals(listOf(1, 1, 1, 2, 2, 3, 3), listOf(0, 1, 3, 4, 6, 7, 100).map(::productColumnCount))
    }

    @Test fun leapDayAppearsOnlyInLeapYears() {
        assertEquals(29, calendarDays(YearMonth.of(2024, 2)).filterNotNull().size)
        assertEquals(28, calendarDays(YearMonth.of(2023, 2)).filterNotNull().size)
        assertTrue(calendarDays(YearMonth.of(2024, 2)).contains(LocalDate.of(2024, 2, 29)))
    }

    @Test fun calendarStartsOnMondayAndKeepsSixWeeks() {
        val days = calendarDays(YearMonth.of(2026, 2))
        assertEquals(42, days.size)
        assertTrue(days.take(6).all { it == null })
        assertEquals(LocalDate.of(2026, 2, 1), days[6])
        assertEquals(LocalDate.of(2026, 2, 28), days[33])
        assertTrue(days.drop(34).all { it == null })
    }

    @Test fun navigationCannotLeavePurchaseDateRange() {
        val today = LocalDate.of(2026, 10, 2)
        assertEquals(YearMonth.of(1970, 1), boundedMonth(YearMonth.of(1969, 12), today))
        assertEquals(YearMonth.of(2026, 10), boundedMonth(YearMonth.of(2026, 12), today))
        assertEquals(YearMonth.of(2025, 12), boundedMonth(YearMonth.of(2025, 12), today))
    }

    @Test fun monthArrowsCanCrossYearBoundary() {
        val today = LocalDate.of(2026, 10, 2)
        assertEquals(YearMonth.of(2025, 12), boundedMonth(YearMonth.of(2026, 1).minusMonths(1), today))
        assertEquals(YearMonth.of(2026, 1), boundedMonth(YearMonth.of(2025, 12).plusMonths(1), today))
    }
}
