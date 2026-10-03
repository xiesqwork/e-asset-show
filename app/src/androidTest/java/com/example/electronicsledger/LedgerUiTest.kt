package com.example.electronicsledger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class LedgerUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun addAndEditProduct() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("electronics.db")
        compose.setContent { LedgerTheme { LedgerApp() } }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("添加产品").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("添加产品").performClick()
        compose.onNodeWithText("产品名称").performTextInput("测试耳机")
        compose.onNodeWithText("买入价格").performTextInput("100.00")
        compose.onNodeWithText("外设").performClick()
        compose.onNodeWithText("保存产品").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("我的产品").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("编辑").assertDoesNotExist()
        compose.onNodeWithTag("product-grid-1").performScrollToNode(hasContentDescription("测试耳机更多操作"))
        compose.onNodeWithContentDescription("测试耳机更多操作").performClick()
        compose.onNodeWithText("删除").assertIsDisplayed()
        compose.onNodeWithText("编辑").performClick()
        compose.onNodeWithText("买入价格").performTextReplacement("50.00")
        compose.onNodeWithText("保存产品").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("我的产品").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("买入 ¥ 50.00").assertExists()
    }
}
