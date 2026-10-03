package com.example.electronicsledger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalendarUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun yearAndMonthArrowsNavigateIndependently() {
        var result: LocalDate? = null
        compose.setContent { LedgerTheme {
            PurchaseDateDialog(LocalDate.of(2024, 2, 29), LocalDate.of(2026, 10, 2), {}, { result = it })
        } }
        compose.onNodeWithContentDescription("上一年").performClick()
        compose.onNodeWithText("2023 年").assertExists()
        compose.onNodeWithContentDescription("选择 2023-02-29").assertDoesNotExist()
        compose.onNodeWithContentDescription("下一年").performClick()
        compose.onNodeWithText("2024 年").assertExists()
        compose.onNodeWithContentDescription("上个月").performClick()
        compose.onNodeWithText("1 月").assertExists()
        compose.onNodeWithContentDescription("下个月").performClick()
        compose.onNodeWithContentDescription("选择 2024-02-29").performClick()
        compose.onNodeWithText("确定").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(LocalDate.of(2024, 2, 29), result) }
    }

    @Test fun cannotChooseFutureDateOrNavigateBefore1970() {
        compose.setContent { LedgerTheme {
            PurchaseDateDialog(LocalDate.of(1970, 1, 1), LocalDate.of(2026, 10, 2), {}, {})
        } }
        compose.onNodeWithContentDescription("上一年").assertIsNotEnabled()
        compose.onNodeWithContentDescription("上个月").assertIsNotEnabled()
        compose.onNodeWithText("回到今天").performScrollTo().performClick()
        compose.onNodeWithContentDescription("下一年").assertIsNotEnabled()
        compose.onNodeWithContentDescription("下个月").assertIsNotEnabled()
        compose.onNodeWithContentDescription("选择 2026-10-03").assertIsNotEnabled()
    }
}
