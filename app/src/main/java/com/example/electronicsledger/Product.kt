package com.example.electronicsledger

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Category(val label: String) {
    PHONE("手机"), COMPUTER("电脑"), PERIPHERAL("外设"), OTHER("其他")
}

data class Product(
    val id: Long = 0,
    val name: String,
    val priceCents: Long,
    val category: Category,
    val purchasedOn: LocalDate
) {
    fun daysOwned(today: LocalDate): Long = (ChronoUnit.DAYS.between(purchasedOn, today) + 1).coerceAtLeast(1)
    fun dailyCost(today: LocalDate): BigDecimal = BigDecimal.valueOf(priceCents, 2)
        .divide(BigDecimal.valueOf(daysOwned(today)), 2, RoundingMode.HALF_UP)
}

fun parsePriceCents(input: String): Long? {
    val text = input.trim()
    if (!Regex("[0-9]{1,9}(\\.[0-9]{1,2})?").matches(text)) return null
    return runCatching { BigDecimal(text).movePointRight(2).longValueExact() }.getOrNull()
}

fun priceText(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString()

fun validateProduct(name: String, price: String, date: LocalDate, today: LocalDate): String? = when {
    name.isBlank() -> "请输入产品名称"
    name.trim().length > 60 -> "产品名称最多 60 个字"
    parsePriceCents(price) == null -> "请输入有效价格，最多 9 位整数和 2 位小数"
    date > today -> "购买日期不能晚于今天"
    date < LocalDate.of(1970, 1, 1) -> "购买日期不能早于 1970 年"
    else -> null
}
