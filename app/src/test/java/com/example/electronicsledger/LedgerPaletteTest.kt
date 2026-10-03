package com.example.electronicsledger

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerPaletteTest {
    @Test fun absentOrUnknownSavedPaletteFallsBackToIndigo() {
        assertEquals(LedgerPalette.INDIGO, LedgerPalette.fromId(null))
        assertEquals(LedgerPalette.INDIGO, LedgerPalette.fromId("removed-palette"))
        LedgerPalette.entries.forEach { assertEquals(it, LedgerPalette.fromId(it.id)) }
        assertEquals(10, LedgerPalette.entries.map { it.id }.distinct().size)
    }

    @Test fun textAndButtonsRemainReadableInEveryPalette() {
        LedgerPalette.entries.forEach { palette ->
            val c = palette.colors
            val pairs = mapOf(
                "page text" to (c.onBackground to c.background),
                "card text" to (c.onSurface to c.surface),
                "secondary text" to (c.onSurfaceVariant to c.surface),
                "page secondary text" to (c.onSurfaceVariant to c.background),
                "dialog secondary text" to (c.onSurfaceVariant to c.surfaceContainerHigh),
                "variant secondary text" to (c.onSurfaceVariant to c.surfaceVariant),
                "dialog action" to (c.primary to c.surfaceContainerHigh),
                "amount" to (c.primary to c.surface),
                "button" to (c.onPrimary to c.primary),
                "summary" to (c.onPrimaryContainer to c.primaryContainer)
            )
            pairs.forEach { (role, pair) ->
                val ratio = contrast(pair.first, pair.second)
                assertTrue("${palette.label} $role contrast is $ratio", ratio >= 4.5f)
            }
        }
    }

    private fun contrast(first: Color, second: Color): Float {
        val a = first.luminance()
        val b = second.luminance()
        return (maxOf(a, b) + .05f) / (minOf(a, b) + .05f)
    }
}
