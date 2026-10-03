package com.example.electronicsledger

import java.time.LocalDate
import java.time.YearMonth

internal val EarliestPurchaseDate: LocalDate = LocalDate.of(1970, 1, 1)

internal fun productColumnCount(count: Int): Int = when {
    count <= 3 -> 1
    count <= 6 -> 2
    else -> 3
}

internal fun boundedMonth(month: YearMonth, today: LocalDate): YearMonth =
    month.coerceIn(YearMonth.from(EarliestPurchaseDate), YearMonth.from(today))

/** Monday first; 42 cells keep the dialog height stable across months. */
internal fun calendarDays(month: YearMonth): List<LocalDate?> {
    val offset = month.atDay(1).dayOfWeek.value - 1
    return List(42) { index ->
        val day = index - offset + 1
        if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
    }
}
