package com.example.electronicsledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun PurchaseDateDialog(
    initialDate: LocalDate,
    today: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var selectedText by rememberSaveable { mutableStateOf(initialDate.coerceIn(EarliestPurchaseDate, today).toString()) }
    var monthText by rememberSaveable { mutableStateOf(boundedMonth(YearMonth.from(initialDate), today).toString()) }
    val selected = LocalDate.parse(selectedText)
    val month = boundedMonth(YearMonth.parse(monthText), today)
    val minimumMonth = YearMonth.from(EarliestPurchaseDate)
    val maximumMonth = YearMonth.from(today)

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
                Text("选择买入日期", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("已选 $selected", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                DateStepper(
                    label = "${month.year} 年", previousLabel = "上一年", nextLabel = "下一年",
                    canPrevious = month.year > minimumMonth.year, canNext = month.year < maximumMonth.year,
                    onPrevious = { monthText = boundedMonth(month.minusYears(1), today).toString() },
                    onNext = { monthText = boundedMonth(month.plusYears(1), today).toString() }
                )
                Spacer(Modifier.height(8.dp))
                DateStepper(
                    label = "${month.monthValue} 月", previousLabel = "上个月", nextLabel = "下个月",
                    canPrevious = month > minimumMonth, canNext = month < maximumMonth,
                    onPrevious = { monthText = boundedMonth(month.minusMonths(1), today).toString() },
                    onNext = { monthText = boundedMonth(month.plusMonths(1), today).toString() }
                )
                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                    listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                calendarDays(month).chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            if (day == null) Spacer(Modifier.weight(1f).aspectRatio(1f))
                            else {
                                val enabled = day in EarliestPurchaseDate..today
                                Surface(
                                    onClick = { selectedText = day.toString() }, enabled = enabled,
                                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                        .semantics { contentDescription = "选择 $day" },
                                    shape = CircleShape,
                                    color = if (day == selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    border = if (day == today && day != selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(day.dayOfMonth.toString(), fontSize = 14.sp,
                                            color = if (!enabled) MaterialTheme.colorScheme.outlineVariant else if (day == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = { selectedText = today.toString(); monthText = maximumMonth.toString() }) { Text("回到今天") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
                    Button(onClick = { onConfirm(selected) }, enabled = selected in EarliestPurchaseDate..today, modifier = Modifier.weight(1f)) { Text("确定") }
                }
            }
        }
    }
}

@Composable
private fun DateStepper(
    label: String, previousLabel: String, nextLabel: String,
    canPrevious: Boolean, canNext: Boolean, onPrevious: () -> Unit, onNext: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onPrevious, enabled = canPrevious) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, previousLabel) }
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            IconButton(onClick = onNext, enabled = canNext) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, nextLabel) }
        }
    }
}
