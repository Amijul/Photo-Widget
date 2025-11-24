package com.amijul.photowidget.presentation.editor


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amijul.photowidget.widget.domain.TextAlignment
import com.amijul.photowidget.widget.domain.TextState


/**
 * Text editor surface shown when mode = TEXT.
 *
 * @param state Current text state from ViewModel.
 * @param onTextChange Called when user edits the main note content.
 * @param onStyleChange Called when text style changes (font, bold, align, size).
 * @param onReset Called when user presses Reset to restore default text+style.
 */
@Composable
fun TextEditorScreen(
    state: TextState,
    onTextChange: (String) -> Unit,
    onStyleChange: (
        fontFamilyKey: String?,
        isBold: Boolean?,
        alignment: TextAlignment?,
        fontSizeSp: Float?
    ) -> Unit,
    onReset: () -> Unit
) {
    // Local snapshot for user-friendly UI controls
    var content by remember(state) { mutableStateOf(state.content) }
    var selectedFontKey by remember(state) { mutableStateOf(state.style.fontFamilyKey) }
    var isBold by remember(state) { mutableStateOf(state.style.isBold) }
    var alignment by remember(state) { mutableStateOf(state.style.alignment) }
    var fontSize by remember(state) { mutableStateOf(state.style.fontSizeSp) }

    // Sync when VM updates state externally (e.g. reset)
    LaunchedEffect(state) {
        content = state.content
        selectedFontKey = state.style.fontFamilyKey
        isBold = state.style.isBold
        alignment = state.style.alignment
        fontSize = state.style.fontSizeSp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(16.dp)
    ) {

        Text(
            text = "Text",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main sticky-note card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                    )
            ) {
                val textAlign = when (alignment) {
                    TextAlignment.LEFT -> androidx.compose.ui.text.style.TextAlign.Left
                    TextAlignment.CENTER -> androidx.compose.ui.text.style.TextAlign.Center
                    TextAlignment.RIGHT -> androidx.compose.ui.text.style.TextAlign.Right
                }

                val fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal

                val baseStyle = TextStyle(
                    fontSize = fontSize.sp,
                    fontWeight = fontWeight,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = textAlign
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { new ->
                        content = new
                        onTextChange(new)
                    },
                    modifier = Modifier
                        .fillMaxSize(),
                    textStyle = baseStyle,
                    placeholder = {
                        Text(
                            "Write your note…",
                            style = baseStyle.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    },
                    singleLine = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls panel (font family, bold, align, size, reset)
        TextControlPanel(
            selectedFontKey = selectedFontKey,
            isBold = isBold,
            alignment = alignment,
            fontSize = fontSize,
            onFontKeyChange = { key ->
                selectedFontKey = key
                onStyleChange(key, null, null, null)
            },
            onBoldToggle = {
                val newBold = !isBold
                isBold = newBold
                onStyleChange(null, newBold, null, null)
            },
            onAlignmentChange = { align ->
                alignment = align
                onStyleChange(null, null, align, null)
            },
            onFontSizeChange = { size ->
                fontSize = size
                onStyleChange(null, null, null, size)
            },
            onReset = onReset
        )
    }
}

@Composable
private fun TextControlPanel(
    selectedFontKey: String,
    isBold: Boolean,
    alignment: TextAlignment,
    fontSize: Float,
    onFontKeyChange: (String) -> Unit,
    onBoldToggle: () -> Unit,
    onAlignmentChange: (TextAlignment) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Font family selector (simple chips based on string keys)
            Text(
                text = "Font",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            val fontOptions = listOf(
                "default" to "Default",
                "serif" to "Serif",
                "sans" to "Sans",
                "mono" to "Mono"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fontOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFontKey == key,
                        onClick = { onFontKeyChange(key) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Bold + Alignment row
            Text(
                text = "Style",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bold toggle
                IconToggleButton(
                    checked = isBold,
                    onCheckedChange = { onBoldToggle() }
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = "Bold"
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlignIconButton(
                        icon = Icons.Default.FormatAlignLeft,
                        selected = alignment == TextAlignment.LEFT,
                        onClick = { onAlignmentChange(TextAlignment.LEFT) }
                    )
                    AlignIconButton(
                        icon = Icons.Default.FormatAlignCenter,
                        selected = alignment == TextAlignment.CENTER,
                        onClick = { onAlignmentChange(TextAlignment.CENTER) }
                    )
                    AlignIconButton(
                        icon = Icons.Default.FormatAlignRight,
                        selected = alignment == TextAlignment.RIGHT,
                        onClick = { onAlignmentChange(TextAlignment.RIGHT) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Font size slider
            Text(
                text = "Font size",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))

            Slider(
                value = fontSize,
                onValueChange = { onFontSizeChange(it) },
                valueRange = 12f..28f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Small", style = MaterialTheme.typography.bodySmall)
                Text("${fontSize.toInt()}sp", style = MaterialTheme.typography.bodySmall)
                Text("Large", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            // Reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onReset) {
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun AlignIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}
