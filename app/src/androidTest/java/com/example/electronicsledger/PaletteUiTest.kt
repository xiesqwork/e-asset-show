package com.example.electronicsledger

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowInsetsControllerCompat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PaletteUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private var original: LedgerPalette? = null

    @After fun restorePreference() {
        original?.let { ThemePreferences(compose.activity).save(it) }
    }

    @Test fun allPalettesApplyImmediatelyAndSelectionSurvivesRecreation() {
        val preferences = ThemePreferences(compose.activity)
        original = preferences.load()
        compose.onNodeWithContentDescription("切换配色").performClick()
        LedgerPalette.entries.forEach { palette ->
            compose.onNodeWithTag("palette-${palette.id}").performScrollTo().performClick().assertIsSelected()
            compose.runOnIdle {
                assertEquals(palette, ThemePreferences(compose.activity).load())
                val window = compose.activity.window
                val bars = WindowInsetsControllerCompat(window, window.decorView)
                assertEquals(!palette.isDark, bars.isAppearanceLightStatusBars)
                assertEquals(!palette.isDark, bars.isAppearanceLightNavigationBars)
            }
        }
        compose.onNodeWithText("完成").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithContentDescription("切换配色").performClick()
        compose.onNodeWithTag("palette-obsidian").performScrollTo().assertIsSelected()
        compose.onNodeWithText("完成").performClick()
        compose.onNodeWithContentDescription("切换配色").assertIsDisplayed()
    }
}
