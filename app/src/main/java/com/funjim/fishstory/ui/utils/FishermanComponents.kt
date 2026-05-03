package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.theme.FishstoryTheme

@Composable
fun FishermanItem(
    fisherman: FishermanSummary,
    index: Int = 0,
    totalItems: Int = 0,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val tripCount = fisherman.totalTrips
    val caughtCount = fisherman.totalCatches
    val releasedCount = fisherman.totalReleased
    val keptCount = caughtCount - releasedCount

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    fisherman.fisherman.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary)
                if (tripCount != 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Trip",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "$tripCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (caughtCount != 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.LeapingFish2,
                            contentDescription = "Fish",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        BoldingNumbersText(
                            text = "Kept $keptCount of $caughtCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
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
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Groups,
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
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center, // Centers the text within the middle space
                )
                Text(
                    "$tackleBoxCount ${if (tackleBoxCount == 1) "tackle box" else "tackle boxes"} assigned",
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center, // Centers the text within the middle space
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                Icons.Default.Inventory,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FishermanItemPreview() {
    FishstoryTheme {
        FishermanItem(
            fisherman = FishermanSummary(
                fisherman = Fisherman(firstName = "John", lastName = "Doe", nickname = "Big Fish"),
                totalCatches = 10,
                totalReleased = 2,
                totalTrips = 5
            ),
            onDelete = {},
            onClick = {}
        )
    }
}
