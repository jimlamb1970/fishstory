package com.funjim.fishstory.ui.utils

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.flow.Flow
import androidx.core.graphics.toColorInt
import com.funjim.fishstory.model.LureSummaryWithColors

@Composable
fun LureItem(
    item: LureSummaryWithColors,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    var menuExpanded by remember { mutableStateOf(false) }

    val backgroundColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    val borderColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Lure
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((4).dp)
                ) {
                    Text(
                        text = item.lureSummary.lure.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    //LureColorComposition(item.primaryColor?.hexCode, item.secondaryColor?.hexCode, item.glowColor?.hexCode)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((4).dp)
                ) {
                    if (item.primaryColor != null) {
                        if (item.primaryColor.hexCode.isNullOrBlank()) {
                            Text(
                                text = item.primaryColor.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            ColorCircleBadge(
                                hexCode = item.primaryColor.hexCode,
                                label = "P"
                            )
                        }
                    }

                    if (item.secondaryColor != null) {
                        if (item.secondaryColor.hexCode.isNullOrBlank()) {
                            Text(
                                text = item.secondaryColor.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            ColorCircleBadge(
                                hexCode = item.secondaryColor.hexCode,
                                label = "S"
                            )
                        }
                    }

                    if (item.lureSummary.lure.glows) {
                        val sb = StringBuilder("Glows")
                        if (item.glowColor != null) {
                            if (item.glowColor.hexCode.isNullOrBlank()) {
                                sb.append(": ${item.glowColor.name}")
                                Text(
                                    text = sb.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                ColorCircleBadge(
                                    hexCode = item.glowColor.hexCode,
                                    label = "G",
                                    isGlow = true
                                )
                            }
                        } else {
                            Text(
                                text = sb.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Text(
                    text = if (item.lureSummary.lure.hasSingleHook) "Single Hook" else "Multiple Hooks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.lureSummary.caughtCount != 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.LeapingFish,
                            contentDescription = "Fish",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        BoldingNumbersText(
                            text = "Kept ${item.lureSummary.keptCount} of ${item.lureSummary.caughtCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Box {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LureColorComposition(
    primaryHex: String?,
    secondaryHex: String?,
    glowHex: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-6).dp) // Negative spacing overlaps them elegantly
    ) {
        // 1. Primary Color (Bottom layer if overlapping)
        if (!primaryHex.isNullOrBlank()) {
            ColorCircleBadge(hexCode = primaryHex, label = "P")
        }

        // 2. Secondary Color
        if (!secondaryHex.isNullOrBlank()) {
            ColorCircleBadge(hexCode = secondaryHex, label = "S")
        }

        // 3. Glow Color (With a subtle dashed border to indicate it glows)
        if (!glowHex.isNullOrBlank()) {
            ColorCircleBadge(
                hexCode = glowHex,
                label = "G",
                isGlow = true
            )
        }
    }
}

@Composable
fun ColorCircleBadge(
    hexCode: String,
    label: String,
    modifier: Modifier = Modifier,
    isGlow: Boolean = false
) {
    val color = remember(hexCode) {
        try { Color(hexCode.toColorInt()) } catch (e: Exception) { Color.Gray }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isGlow) 1.5.dp else 2.dp,
                color = if (isGlow) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
    ) {
        // Optional: Put a tiny, high-contrast letter inside so colorblind users know which is which
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isColorDark(color)) Color.White else Color.Black,
            modifier = Modifier.alpha(0.9f)
        )
    }
}

// Quick helper to determine if text should be white or black inside the circle
fun isColorDark(color: Color): Boolean {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return luminance < 0.5
}