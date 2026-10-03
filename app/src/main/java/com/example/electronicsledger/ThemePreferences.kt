package com.example.electronicsledger

import android.content.Context
import androidx.core.content.edit

internal class ThemePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    fun load(): LedgerPalette = LedgerPalette.fromId(preferences.getString("palette", null))

    fun save(palette: LedgerPalette) {
        preferences.edit { putString("palette", palette.id) }
    }
}
