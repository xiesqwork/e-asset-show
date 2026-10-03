package com.example.electronicsledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun PaletteDialog(selected: LedgerPalette, onSelect: (LedgerPalette) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择配色") },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()).selectableGroup()) {
                LedgerPalette.entries.forEach { palette ->
                    Row(
                        Modifier.fillMaxWidth().testTag("palette-${palette.id}")
                            .selectable(selected = palette == selected, role = Role.RadioButton, onClick = { onSelect(palette) })
                            .heightIn(min = 60.dp).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(selected = palette == selected, onClick = null)
                        Column(Modifier.weight(1f)) {
                            Text(palette.label)
                            if (palette.isDark) Text("深色", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf(palette.colors.background, palette.colors.primaryContainer, palette.colors.primary).forEach { color ->
                                Surface(Modifier.size(16.dp), shape = CircleShape, color = color,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {}
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } }
    )
}
