package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.FishSummary
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BodyOfWaterSelectionField
import com.funjim.fishstory.ui.utils.EventSelectionField
import com.funjim.fishstory.ui.utils.FishermanSelectionField
import com.funjim.fishstory.ui.utils.LureSelectionField
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.StatItem
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.TripSelectionField
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.viewmodels.FishViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishSummaryScreen(
    viewModel: FishViewModel,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, eventId: String, fishId: String?) -> Unit,
    onNavigateToFishList: (String?, String?, String?, String?, String?, String?, Boolean) -> Unit
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    val allBodiesOfWater by viewModel.bodiesOfWaterWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedBodyOfWater = remember(allBodiesOfWater, filter.bodyOfWaterId) {
        allBodiesOfWater.find { it.id == filter.bodyOfWaterId }
    }

    val trips by viewModel.tripsWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTrip = remember(trips, filter.tripId) {
        trips.find { it.id == filter.tripId }
    }

    val events by viewModel.eventsWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedEvent = remember(events, filter.eventId) {
        events.find { it.id == filter.eventId }
    }

    val fishermen by viewModel.fishermenWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedFisherman = remember(fishermen, filter.fishermanId) {
        fishermen.find { it.id == filter.fishermanId }
    }

    val lures by viewModel.luresWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedLure = remember(lures, filter.lureId) {
        lures.find { it.lure.id == filter.lureId }
    }

    val species by viewModel.speciesWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedSpecies = remember(species, filter.speciesId) {
        species.find { it.id == filter.speciesId }
    }

    val summary by viewModel.fishSummary.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fish Caught") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSelections()
                        navigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (selectedEvent != null) {
                        IconButton(
                            onClick = { onAddFish(selectedEvent.tripId, selectedEvent.id, null) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    AppIcons.Default.LeapingFishWithFins,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp) // Adjust offset to position on the edge
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Pinned Area: FishVisual is placed outside of the LazyColumn so it never scrolls out of view.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                FishVisual(
                    summary = summary,
                    trip = selectedTrip,
                    event = selectedEvent,
                    species = selectedSpecies,
                    fisherman = selectedFisherman,
                    lure = selectedLure,
                    bodyOfWater = selectedBodyOfWater,
                    onClick = {
                        onNavigateToFishList(
                            filter.bodyOfWaterId,
                            filter.eventId,
                            filter.fishermanId,
                            filter.lureId,
                            filter.speciesId,
                            filter.tripId,
                            false
                        )
                    },
                    onFishClick = {
                        onNavigateToFishList(
                            filter.bodyOfWaterId,
                            filter.eventId,
                            filter.fishermanId,
                            filter.lureId,
                            filter.speciesId,
                            filter.tripId,
                            false
                        )
                    },
                    onTargetFishClick = {
                        onNavigateToFishList(
                            filter.bodyOfWaterId,
                            filter.eventId,
                            filter.fishermanId,
                            filter.lureId,
                            filter.speciesId,
                            filter.tripId,
                            true
                        )
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Scrollable Area: Only the selection fields scroll under the visual summary.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TripSelectionField(
                        items = trips,
                        selectedItem = selectedTrip,
                        onSelected = { trip ->
                            viewModel.selectTrip(trip.id)
                            viewModel.selectEvent(null)
                        },
                        onClear = {
                            viewModel.selectTrip(null)
                            viewModel.selectEvent(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { trip ->
                            val thumbnailFlow = remember(trip.id) {
                                viewModel.tripThumbnail(trip.id)
                            }

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.Boat,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                }

                item {
                    EventSelectionField(
                        items = events,
                        selectedItem = selectedEvent,
                        onSelected = { event ->
                            viewModel.selectEvent(event.id)
                        },
                        onClear = {
                            viewModel.selectEvent(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { event ->
                            val thumbnailFlow = remember(event.id) {
                                viewModel.eventThumbnail(event.id)
                            }

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.Boat,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                }

                item {
                    SpeciesSelection(
                        items = species,
                        selectedItem = selectedSpecies,
                        onSelected = { species ->
                            viewModel.selectSpecies(species.id) },
                        modifier = Modifier.fillMaxWidth(),
                        onClear = {
                            viewModel.selectSpecies(null)
                        },
                        thumbnailProvider = { species ->
                            val thumbnailFlow = remember(species.id) {
                                viewModel.speciesThumbnail(species.id)
                            }

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.Species,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                }

                item {
                    FishermanSelectionField(
                        items = fishermen,
                        selectedItem = selectedFisherman,
                        onSelected = { fisherman ->
                            viewModel.selectFisherman(fisherman.id)
                        },
                        onClear = {
                            viewModel.selectFisherman(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { fisherman ->
                            val thumbnailFlow = remember(fisherman.id) {
                                viewModel.fishermanThumbnail(fisherman.id)
                            }

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.Fisherman,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                }

                item {
                    LureSelectionField(
                        items = lures,
                        selectedItem = selectedLure,
                        onSelected = { lure -> viewModel.selectLure(lure.lure.id) },
                        onClear = { viewModel.selectLure(null) },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { lure ->
                            val thumbnailFlow = remember(lure.lure.id) {
                                viewModel.lureThumbnail(lure.lure.id)
                            }

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.Lure,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                }

                item {
                    BodyOfWaterSelectionField(
                        items = allBodiesOfWater,
                        selectedItem = selectedBodyOfWater,
                        onSelected = { bodyOfWater -> viewModel.selectBodyOfWater(bodyOfWater.id) },
                        onClear = { viewModel.selectBodyOfWater(null) },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { bodyOfWater ->
                            val thumbnailFlow = remember(bodyOfWater.id) {
                                viewModel.lureThumbnail(bodyOfWater.id)
                            }

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.BodyOfWater,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FishVisual(
    summary: FishSummary,
    trip: Trip?,
    event: Event?,
    species: Species?,
    fisherman: Fisherman?,
    lure: LureWithColors?,
    bodyOfWater: BodyOfWater?,
    onClick: () -> Unit,
    onFishClick: () -> Unit,
    onTargetFishClick: () -> Unit
) {
    val context = LocalContext.current

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val startDate = event?.startTime ?: trip?.startDate
    val endDate = event?.endTime ?: trip?.endDate
    val hasLocation = if (event != null) {
        event.latitude != null && event.longitude != null
    } else if (trip != null) {
        trip.latitude != null && trip.longitude != null
    } else {
        false
    }

    val latitude = event?.latitude ?: trip?.latitude
    val longitude = event?.longitude ?: trip?.longitude

    val names = listOfNotNull(
        trip?.name,
        event?.name,
        species?.name,
        bodyOfWater?.name,
        fisherman?.fullName,
        lure?.lure?.name)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCardColor().copy(alpha = 0.15f),
            contentColor = getOnCardColor()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Fish Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getOnCardColor(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(
                        label = "CAUGHT",
                        value = "${summary.counts.totalCaught}",
                        labelColor = getOnCardSecondaryColor(),
                        color = getOnCardColor())

                    IconButton(
                        onClick = { onFishClick() }
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.LeapingFishWithFins,
                            contentDescription = "Fish",
                            modifier = Modifier.size(48.dp),
                            tint = getOnCardColor()
                        )
                    }

                    StatItem(
                        label = "KEPT",
                        value = "${summary.counts.totalKept}",
                        labelColor = getOnCardSecondaryColor(),
                        color = getOnCardColor())
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = getOnCardColor().copy(alpha = 0.75f)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(
                        label = "CAUGHT",
                        value = "${summary.counts.totalTargetCaught}",
                        labelColor = getOnCardSecondaryColor(),
                        color = getOnCardColor())

                    IconButton(
                        onClick = { onTargetFishClick() }
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.TargetFish,
                            contentDescription = "Fish",
                            modifier = Modifier.size(48.dp),
                            tint = getOnCardColor()
                        )
                    }

                    StatItem(
                        label = "KEPT",
                        value = "${summary.counts.totalTargetKept}",
                        labelColor = getOnCardSecondaryColor(),
                        color = getOnCardColor())
                }

                if (names.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = getOnCardColor().copy(alpha = 0.75f)
                    )
                }

                names.forEachIndexed { index, string ->
                    if (index == 0) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(0.dp)) {
                            Text(
                                text = string,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = getOnCardColor()
                            )

                            if (hasLocation) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = getOnCardColor(),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            val mapUri =
                                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}")
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
                    } else {
                        Text(
                            text = string,
                            style = MaterialTheme.typography.titleMedium,
                            color = getOnCardColor(),
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }

                if (startDate != null && endDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
                    ) {
                        Text(
                            "${dateFormatter.format(Date(startDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = getOnCardSecondaryColor()
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            tint = getOnCardSecondaryColor(),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "${dateFormatter.format(Date(endDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = getOnCardSecondaryColor()
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = getOnCardColor().copy(alpha = 0.75f)
                )

                Text(
                    text = "Tap to view fish",
                    style = MaterialTheme.typography.labelSmall,
                    color = getOnCardColor().copy(alpha = 0.75f)
                )
            }
        }
    }
}