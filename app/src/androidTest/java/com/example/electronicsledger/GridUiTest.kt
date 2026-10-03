package com.example.electronicsledger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GridUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun productCountChangesActualGridColumns() {
        val context = compose.activity.application
        context.deleteDatabase("electronics.db")
        val database = ProductDatabase(context)
        val model = LedgerViewModel(context)
        compose.setContent { LedgerTheme { LedgerApp(model) } }
        try {
            for (count in listOf(3, 4, 6, 7)) {
                database.changeAndRead {
                    database.all().forEach { database.delete(it.id) }
                    repeat(count) { index -> database.save(Product(name = "产品 $index", priceCents = 10000,
                        category = Category.PHONE, purchasedOn = LocalDate.now().minusDays(index.toLong()))) }
                }
                compose.runOnIdle { model.reload() }
                val columns = productColumnCount(count)
                compose.waitUntil(10_000) { compose.onAllNodesWithTag("product-grid-$columns").fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithTag("product-grid-$columns").performScrollToNode(hasText("产品 0"))
                if (columns > 1) {
                    val first = compose.onNodeWithText("产品 0").fetchSemanticsNode().boundsInRoot
                    val second = compose.onNodeWithText("产品 1").fetchSemanticsNode().boundsInRoot
                    assertTrue(second.left > first.left)
                    assertTrue(kotlin.math.abs(first.top - second.top) < 2f)
                    if (columns == 3) {
                        val third = compose.onNodeWithText("产品 2").fetchSemanticsNode().boundsInRoot
                        assertTrue(third.left > second.left)
                        assertTrue(kotlin.math.abs(first.top - third.top) < 2f)
                    }
                }
            }
        } finally { database.close() }
    }
}
