package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.ui.utils.toInches
import com.funjim.fishstory.viewmodels.MainViewModel

// --- Vico 3.x imports ---
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.Instant

import java.time.ZoneId

import kotlin.math.min
import kotlin.math.roundToInt

enum class ReportType(val label: String) {
    SPECIES("Catches by Species"),
    FISHERMAN("Catches by Fisherman"),
    FISHERMAN_PIE("Catches by Fisherman (Pie)"),
    FISHERMAN_SPECIES("Catches by Fisherman & Species"),
    CATCHES_BY_HOUR("Catches by Hour"),
    CATCHES_BY_SIZE("Catches by Size")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OldReportsScreen(
    viewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    val allTrips by viewModel.trips.collectAsStateWithLifecycle(initialValue = emptyList())

    // Selected trip/segment IDs — local to this screen, not shared with FishListScreen
    var selectedTripId by remember { mutableStateOf<String?>(null) }
    var selectedSegmentId by remember { mutableStateOf<String?>(null) }

    val selectedTrip = remember(allTrips, selectedTripId) {
        allTrips.find { it.id == selectedTripId }
    }

    val segmentsForTrip by produceState<List<Event>>(initialValue = emptyList(), key1 = selectedTripId) {
        selectedTrip?.let { trip ->
            viewModel.getSegmentsForTrip(trip.id).collect { value = it }
        } ?: run { value = emptyList() }
    }

    val selectedSegment = remember(segmentsForTrip, selectedSegmentId) {
        segmentsForTrip.find { it.id == selectedSegmentId }
    }

    // Fish scoped to the current selection
    val fishList by produceState<List<FishWithDetails>>(
        initialValue = emptyList(),
        key1 = selectedTripId,
        key2 = selectedSegmentId
    ) {
        when {
            selectedSegmentId != null ->
                viewModel.getFishForSegment(selectedSegmentId!!).collect { value = it }
            selectedTripId != null ->
                viewModel.getFishForTrip(selectedTripId!!).collect { value = it }
            else ->
                viewModel.allFish.collect { value = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catch Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Reports have been deprecated!")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Catch Reports") },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text("No fish logged yet. Log some fish to see reports!")
    }
}
