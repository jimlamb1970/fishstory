package com.funjim.fishstory.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.viewmodels.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishDialog(
    viewModel: MainViewModel,
    speciesList: List<Species>,
    fishermenList: List<Fisherman>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int?, Double, Boolean, Long, Int?) -> Unit,
    onAddSpecies: (String, (Int) -> Unit) -> Unit
) {
    var selectedSpeciesId by remember { mutableStateOf<Int?>(null) }
    var selectedFishermanId by remember { mutableStateOf<Int?>(null) }
    var selectedLureId by remember { mutableStateOf<Int?>(null) }
    var lengthStr by remember { mutableStateOf("") }
    var released by remember { mutableStateOf(true) }
    var holeNumberStr by remember { mutableStateOf("") }
    
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    val rawLures by if (selectedFishermanId != null) {
        viewModel.getLuresForFisherman(selectedFishermanId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Lure>()) }
    }
    
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())
    
    val luresSorted = remember(rawLures, colors) {
        rawLures.map { lure ->
            val colorName = colors.find { it.id == lure.colorId }?.name
            val glowColorName = colors.find { it.id == lure.glowColorId }?.name
            lure to lure.getDisplayName(colorName, glowColorName)
        }.sortedBy { it.second }
    }
    
    var showNewSpeciesDialog by remember { mutableStateOf(false) }
    var newSpeciesName by remember { mutableStateOf("") }
    
    var speciesExpanded by remember { mutableStateOf(false) }
    var fishermanExpanded by remember { mutableStateOf(false) }
    var lureExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.MINUTE)
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = timestamp
                            val hour = get(Calendar.HOUR_OF_DAY)
                            val minute = get(Calendar.MINUTE)
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                        }
                        timestamp = calendar.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = timestamp
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    timestamp = calendar.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showNewSpeciesDialog) {
        AlertDialog(
            onDismissRequest = { 
                showNewSpeciesDialog = false
                newSpeciesName = ""
            },
            title = { Text("Add New Species") },
            text = {
                TextField(
                    value = newSpeciesName,
                    onValueChange = { newSpeciesName = it },
                    placeholder = { Text("Species Name (e.g. Walleye)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newSpeciesName.isNotBlank()) {
                        onAddSpecies(newSpeciesName) { newId ->
                            selectedSpeciesId = newId
                        }
                        showNewSpeciesDialog = false
                        newSpeciesName = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                Button(onClick = { 
                    showNewSpeciesDialog = false
                    newSpeciesName = ""
                }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Catch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Species Dropdown
                ExposedDropdownMenuBox(
                    expanded = speciesExpanded,
                    onExpandedChange = { speciesExpanded = !speciesExpanded }
                ) {
                    val selectedSpeciesName = speciesList.find { it.id == selectedSpeciesId }?.name ?: "Select Species"
                    TextField(
                        value = selectedSpeciesName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Species") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = speciesExpanded,
                        onDismissRequest = { speciesExpanded = false }
                    ) {
                        speciesList.forEach { species ->
                            DropdownMenuItem(
                                text = { Text(species.name) },
                                onClick = {
                                    selectedSpeciesId = species.id
                                    speciesExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Add species...", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                speciesExpanded = false
                                showNewSpeciesDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }

                // Fisherman Dropdown
                ExposedDropdownMenuBox(
                    expanded = fishermanExpanded,
                    onExpandedChange = { fishermanExpanded = !fishermanExpanded }
                ) {
                    val selectedFishermanName = fishermenList.find { it.id == selectedFishermanId }?.fullName ?: "Select Fisherman"
                    TextField(
                        value = selectedFishermanName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Who caught it?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fishermanExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fishermanExpanded,
                        onDismissRequest = { fishermanExpanded = false }
                    ) {
                        fishermenList.forEach { fisherman ->
                            DropdownMenuItem(
                                text = { Text(fisherman.fullName) },
                                onClick = {
                                    selectedFishermanId = fisherman.id
                                    selectedLureId = null // Reset lure when fisherman changes
                                    fishermanExpanded = false
                                }
                            )
                        }
                    }
                }

                // Lure Dropdown (Filtered by Fisherman)
                ExposedDropdownMenuBox(
                    expanded = lureExpanded,
                    onExpandedChange = { if (selectedFishermanId != null) lureExpanded = !lureExpanded }
                ) {
                    val selectedLure = rawLures.find { it.id == selectedLureId }
                    val selectedLureName = if (selectedLure != null) {
                        val colorName = colors.find { it.id == selectedLure.colorId }?.name
                        val glowColorName = colors.find { it.id == selectedLure.glowColorId }?.name
                        selectedLure.getDisplayName(colorName, glowColorName)
                    } else {
                        "Select Lure"
                    }

                    TextField(
                        value = selectedLureName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lure Used") },
                        enabled = selectedFishermanId != null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lureExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = lureExpanded,
                        onDismissRequest = { lureExpanded = false }
                    ) {
                        luresSorted.forEach { (lure, displayName) ->
                            DropdownMenuItem(
                                text = { Text(displayName) },
                                onClick = {
                                    selectedLureId = lure.id
                                    lureExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = lengthStr,
                        onValueChange = { lengthStr = it },
                        label = { Text("Length (in)") },
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = holeNumberStr,
                        onValueChange = { holeNumberStr = it },
                        label = { Text("Hole #") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp)))
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp)))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = released, onCheckedChange = { released = it })
                    Text("Released")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val length = lengthStr.toDoubleOrNull() ?: 0.0
                val holeNum = holeNumberStr.toIntOrNull()
                if (selectedSpeciesId != null && selectedFishermanId != null) {
                    onConfirm(selectedSpeciesId!!, selectedFishermanId!!, selectedLureId, length, released, timestamp, holeNum)
                }
            }) { Text("Log Catch") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun FishItem(
    fish: FishWithDetails, 
    viewModel: MainViewModel,
    onDelete: () -> Unit, 
    onShowMap: () -> Unit, 
    onUpdateLocation: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val fishPhotos by viewModel.getPhotosForFish(fish.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append("${fish.speciesName} - ${fish.length}\"")
                            if (fish.holeNumber != null) {
                                append(" (Hole #${fish.holeNumber})")
                            }
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("Caught by: ${fish.fishermanName}", style = MaterialTheme.typography.bodyMedium)
                    val fullLureName = fish.getFullLureName()
                    if (fullLureName != null) {
                        Text("Lure: $fullLureName", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Status: ${if (fish.isReleased) "Released" else "Kept"}", style = MaterialTheme.typography.bodySmall)
                    Text("At: ${dateFormatter.format(Date(fish.timestamp))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    if (fish.latitude != null && fish.longitude != null) {
                        Text(
                            text = "GPS: ${fish.latitude}, ${fish.longitude}", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.Blue,
                            modifier = Modifier.clickable { onShowMap() }
                        )
                    }
                    
                    TextButton(
                        onClick = onUpdateLocation,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (fish.latitude == null) "Set Location" else "Update Location", style = MaterialTheme.typography.labelMedium)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }

            PhotoPickerRow(
                photos = fishPhotos,
                onPhotoSelected = { uri ->
                    scope.launch {
                        viewModel.addPhoto(Photo(uri = uri.toString(), fishId = fish.id))
                    }
                },
                onPhotoDeleted = { photo ->
                    scope.launch {
                        viewModel.deletePhoto(photo)
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun MapPickerSelectionDialog(
    initialLatLng: LatLng?,
    onDismiss: () -> Unit,
    onConfirm: (LatLng) -> Unit
) {
    var selectedLocation by remember { mutableStateOf(initialLatLng ?: LatLng(45.0, -90.0)) }

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

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        location?.let { it.latitude to it.longitude }
    } catch (e: Exception) {
        null
    }
}
