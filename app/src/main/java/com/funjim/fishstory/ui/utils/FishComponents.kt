package com.funjim.fishstory.ui.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.funjim.fishstory.MapLibreView
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FishItem(
    fish: FishWithDetails,
    index: Int = 0,
    totalItems: Int = 0,
    includeTrip: Boolean = false,
    includeEvent: Boolean = false,
    includeFisherman: Boolean = false,
    thumbnailFlow: Flow<ByteArray?>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetLocation: () -> Unit,
    onSelectLocation: () -> Unit,
    onClearLocation: () -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val dateFormatter = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = getCardColor(index, totalItems),
            contentColor = getCardContentColor()
        ),
        border = BorderStroke(1.dp, color = getCardBorderColor(index, totalItems))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(IntrinsicSize.Min,
                )
            ) {
                ReleasedChip(fish.fish.keptCount == 0)
                Spacer(modifier = Modifier.height(4.dp))
                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.LeapingFish,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            val trip = fish.trip
            val event = fish.event

            val eventLat = event.latitude
            val fishLat = fish.fish.latitude

            // Precedence logic: Use Event if it exists, otherwise use Trip
            val activeLat = fish.fish.latitude ?: event.latitude ?: trip.latitude
            val activeLng = fish.fish.longitude ?: event.longitude ?: trip.longitude

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val lengthStr = if (fish.fish.length != null)
                        " - " + fish.fish.length.toDisplayString(useMetric = false, useFractions = true)
                    else ""

                    Text(
                        text = "${fish.species.name}$lengthStr",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (activeLat != null && activeLng != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = getCardContentColor(),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri =
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${activeLat},${activeLng}")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Could not open map",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        )
                        if (fishLat == null) {
                            Text(
                                text = if (eventLat != null) "(Event)" else "(Trip)",
                                style = MaterialTheme.typography.bodySmall,
                                color = getCardContentColor()
                            )
                        }
                    }
                }

                if (includeTrip)
                    Text(
                        "Trip: ${fish.trip.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getCardSecondaryContentColor()
                    )

                if (includeEvent)
                    Text(
                        "Event: ${fish.event.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getCardSecondaryContentColor()
                    )

                if (includeFisherman)
                    Text(
                        "Caught by: ${fish.fisherman?.fullName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getCardSecondaryContentColor()
                    )

                Text(
                    "Lure: ${fish.fullLureName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = getCardSecondaryContentColor()
                )
                Text(
                    "At: ${dateFormatter.format(Date(fish.fish.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = getCardSecondaryContentColor()
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    contentDescription = null,
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Use Current Location") },
                            onClick = {
                                menuExpanded = false
                                onSetLocation()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint =
                                        if (fish.fish.latitude != null) Color(0xFF4CAF50)
                                        else LocalContentColor.current
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Select on Map") },
                            onClick = {
                                menuExpanded = false
                                onSelectLocation()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint =
                                        if (fish.fish.latitude != null) Color(0xFF4CAF50)
                                        else LocalContentColor.current
                                )
                            }
                        )

                        if (activeLat != null) {
                            DropdownMenuItem(
                                text = {
                                    if (fishLat == null) Text("Reset Location")
                                    else Text("Clear Location")
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClearLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
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
}
