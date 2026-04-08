package com.funjim.fishstory.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.funjim.fishstory.MapLibreView
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
//import com.funjim.fishstory.viewmodels.FishViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FishItem(
    fish: FishWithDetails,
    includeTrip: Boolean = false,
    includeSegment: Boolean = false,
    includeFisherman: Boolean = false,
    photos: List<Photo>,
    onAddPhoto: ((Photo) -> Unit)? = null,
    onDeletePhoto: ((Photo) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onUseSegmentLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                // Only add clickable if the lambda isn't null
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buildString {
                            append("${fish.speciesName} - ${fish.length}\"")
                            /*
                    if (fish.holeNumber != null) {
                        append(" (Hole #${fish.holeNumber})")
                    }
*/
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (fish.latitude != null && fish.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri =
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${fish.latitude},${fish.longitude}")
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
                    }
                }

                if (includeTrip)
                    Text("Trip: ${fish.tripName}", style = MaterialTheme.typography.bodyMedium)

                if (includeSegment)
                    Text("Segment: ${fish.segmentName}", style = MaterialTheme.typography.bodyMedium)

                if (includeFisherman)
                    Text(
                        "Caught by: ${fish.fishermanName}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                val fullLureName = fish.getFullLureName()
                if (fullLureName != null) {
                    Text("Lure: $fullLureName", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "Status: ${if (fish.isReleased) "Released" else "Kept"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "At: ${dateFormatter.format(Date(fish.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (onSetLocation != null) {
                            DropdownMenuItem(
                                text = { Text("Use Current Location") },
                                onClick = {
                                    menuExpanded = false
                                    onSetLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (fish.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                    )
                                }
                            )
                        }

                        if (onSelectLocation != null) {
                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    menuExpanded = false
                                    onSelectLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (fish.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                    )
                                }
                            )
                        }

                        if (onUseTripLocation != null) {
                            DropdownMenuItem(
                                text = { Text("Use Trip Location") },
                                onClick = {
                                    menuExpanded = false
                                    onUseTripLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (fish.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                    )
                                }
                            )
                        }

                        if (onUseSegmentLocation != null) {
                            DropdownMenuItem(
                                text = { Text("Use Segment Location") },
                                onClick = {
                                    menuExpanded = false
                                    onUseSegmentLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (fish.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                    )
                                }
                            )
                        }

                        if (fish.latitude != null && onClearLocation != null) {
                            DropdownMenuItem(
                                text = { Text("Clear Location", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    onClearLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                }
            }
        }

        if (onAddPhoto != null && onDeletePhoto != null) {
            PhotoPickerRow(
                photos = photos,
                onPhotoSelected = { uri ->
                    scope.launch {
                        onAddPhoto(Photo(uri = uri.toString(), fishId = fish.id))
                    }
                },
                onPhotoDeleted = { photo ->
                    scope.launch {
                        onDeletePhoto(photo)
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MapPickerSelectionDialog(
    initialLatLng: LatLng,
    onDismiss: () -> Unit,
    onConfirm: (LatLng) -> Unit
) {
    var selectedLocation by remember { mutableStateOf(initialLatLng) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    MapLibreView(
                        modifier = Modifier.fillMaxSize(),
                        initialLatLng = selectedLocation,
                        onMapClick = { selectedLocation = it },
                        markerPosition = selectedLocation
                    )
                    
                    Text(
                        "Tap map to set location",
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(selectedLocation) }) { Text("Update Location") }
                }
            }
        }
    }
}

@Composable
fun ManageSpeciesDialog(
    species: List<Species>,
    onDismiss: () -> Unit,
    onAddSpecies: (String) -> Unit,
    onDeleteSpecies: (Species) -> Unit
) {
    var newSpeciesName by remember { mutableStateOf("") }

    val sortedSpecies = remember(species) {
        species.sortedBy { it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Species") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newSpeciesName,
                        onValueChange = { newSpeciesName = it },
                        placeholder = { Text("New Species") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newSpeciesName.isNotBlank()) {
                            onAddSpecies(newSpeciesName)
                            newSpeciesName = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(sortedSpecies) { species ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(species.name)
                            IconButton(onClick = { onDeleteSpecies(species) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
