package com.example.electronicsledger

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class LedgerPalette(
    val id: String,
    val label: String,
    val isDark: Boolean,
    background: Long,
    surface: Long,
    ink: Long,
    accent: Long,
    soft: Long,
    muted: Long,
    outline: Long
) {
    GRAPHITE("graphite", "石墨白", false, 0xFFF5F5F5, 0xFFFFFFFF, 0xFF202124, 0xFF41464D, 0xFFE9EAEC, 0xFF62666C, 0xFFE3E4E6),
    MIST_BLUE("mist_blue", "雾蓝灰", false, 0xFFF3F6F8, 0xFFFFFFFF, 0xFF263442, 0xFF4B6B88, 0xFFE3EBF2, 0xFF596876, 0xFFDFE6EC),
    SAGE("sage", "鼠尾草", false, 0xFFF4F6F1, 0xFFFEFFFC, 0xFF29342C, 0xFF526C54, 0xFFE3EBDF, 0xFF5B6859, 0xFFDFE6D9),
    OAT("oat", "暖燕麦", false, 0xFFF6F3ED, 0xFFFFFDFA, 0xFF332F29, 0xFF806548, 0xFFEEE5D7, 0xFF6B6153, 0xFFE8E0D4),
    INDIGO("indigo", "克制靛蓝", false, 0xFFF5F6F9, 0xFFFFFFFF, 0xFF2B3043, 0xFF555FA1, 0xFFE8EAF5, 0xFF60667B, 0xFFE0E3EC),
    GLACIER("glacier", "冰川青", false, 0xFFF1F7F7, 0xFFFFFFFF, 0xFF233A3D, 0xFF3E7277, 0xFFDFEEEE, 0xFF546B6D, 0xFFDCE8E8),
    OCEAN("ocean", "深海蓝白", false, 0xFFF4F6F9, 0xFFFFFFFF, 0xFF243247, 0xFF315A7D, 0xFFE3EBF4, 0xFF5A687D, 0xFFDDE5EE),
    MOCHA("mocha", "摩卡奶油", false, 0xFFF7F2EE, 0xFFFFFCF9, 0xFF392F2A, 0xFF806052, 0xFFEEE1D8, 0xFF6C5F56, 0xFFE9DED6),
    CHARCOAL("charcoal", "炭黑银", true, 0xFF18191B, 0xFF242629, 0xFFF0F1F2, 0xFFB8C1CC, 0xFF353B43, 0xFFB1B7BF, 0xFF41464D),
    OBSIDIAN("obsidian", "黑曜烟紫", true, 0xFF1D1B22, 0xFF2B2832, 0xFFF0EDF5, 0xFFB6A9CC, 0xFF42384F, 0xFFB8B0C4, 0xFF494151);

    val colors: ColorScheme = (if (isDark) darkColorScheme() else lightColorScheme()).copy(
        primary = Color(accent), onPrimary = if (isDark) Color(background) else Color.White,
        primaryContainer = Color(soft), onPrimaryContainer = Color(ink),
        secondary = Color(accent), onSecondary = if (isDark) Color(background) else Color.White,
        secondaryContainer = Color(soft), onSecondaryContainer = Color(ink),
        tertiary = Color(accent), onTertiary = if (isDark) Color(background) else Color.White,
        tertiaryContainer = Color(soft), onTertiaryContainer = Color(ink),
        background = Color(background), onBackground = Color(ink),
        surface = Color(surface), onSurface = Color(ink),
        surfaceVariant = Color(soft), onSurfaceVariant = Color(muted),
        surfaceTint = Color(accent),
        surfaceDim = Color(background), surfaceBright = Color(surface),
        surfaceContainerLowest = Color(background), surfaceContainerLow = Color(surface),
        surfaceContainer = Color(surface), surfaceContainerHigh = Color(surface),
        surfaceContainerHighest = Color(soft),
        outline = Color(muted), outlineVariant = Color(outline),
        inverseSurface = Color(ink), inverseOnSurface = Color(background), inversePrimary = Color(soft)
    )

    companion object {
        fun fromId(id: String?): LedgerPalette = entries.firstOrNull { it.id == id } ?: INDIGO
    }
}

@Composable
fun LedgerTheme(palette: LedgerPalette = LedgerPalette.INDIGO, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette.colors, content = content)
}
