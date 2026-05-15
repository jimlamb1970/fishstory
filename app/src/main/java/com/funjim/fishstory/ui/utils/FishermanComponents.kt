package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.theme.FishstoryTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishermanItem(
    fisherman: FishermanSummary,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val caughtCount = fisherman.totalCatches
    val keptCount = fisherman.totalKept

    var expanded by remember { mutableStateOf(false) }

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
                onClick = { onClick() },
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Fisherman
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    fisherman.fisherman.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // Single Row for both Trip and Catch stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Trip Count
                    if (fisherman.totalTrips != 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Default.Boat,
                                contentDescription = "Trips",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(36.dp) // Increased size
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${fisherman.totalTrips}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Catch Stats
                    if (caughtCount != 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Default.LeapingFish,
                                contentDescription = "Fish",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp) // Increased size
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            BoldingNumbersText(
                                text = "Kept $keptCount of $caughtCount",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Box {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FishermanSummary(
    fishermanCount: Int,
    tackleBoxCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth().padding(16.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                AppIcons.Default.Fisherman,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "THE CREW AND TACKLE BOXES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center, // Centers the text within the middle space
                )
                Text(
                    "$fishermanCount ${if (fishermanCount == 1) "fisherman" else "fishermen"} on board",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center, // Centers the text within the middle space
                )
                Text(
                    "$tackleBoxCount ${if (tackleBoxCount == 1) "tackle box" else "tackle boxes"} assigned",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center, // Centers the text within the middle space
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                AppIcons.Default.TackleBox,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FishermanItemPreview() {
    FishstoryTheme(selectedTheme = null) {
        FishermanItem(
            fisherman = FishermanSummary(
                fisherman = Fisherman(firstName = "John", lastName = "Doe", nickname = "Big Fish"),
                totalCatches = 10,
                totalKept = 2,
                totalTrips = 5
            ),
            thumbnailFlow = flowOf(null),
            onClick = {},
            onDelete = {},
        )
    }
}
