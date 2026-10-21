package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.cards.FishCard
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.LureCompositionWithColors
import com.funjim.fishstory.ui.utils.ReportsList
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getChipColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.FishViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FishViewModel,
    filter: FishFilter,
    navigateBack: () -> Unit
) {
    LaunchedEffect(filter) {
        viewModel.selectBodyOfWater(filter.bodyOfWaterId)
        viewModel.selectFisherman(filter.fishermanId)
        viewModel.selectLure(filter.lureId)
        viewModel.selectSpecies(filter.speciesId)
        viewModel.selectTargetOnly(filter.targetOnly)
        viewModel.selectTrip(filter.tripId, filter.eventId)
        viewModel.selectWater(filter.waterId)
        viewModel.selectWeather(filter.weatherId)
    }

    val bodyOfWater by viewModel.selectedBodyOfWater.collectAsStateWithLifecycle()
    val event by viewModel.selectedEvent.collectAsStateWithLifecycle()
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
    val lure by viewModel.selectedLure.collectAsStateWithLifecycle()
    val species by viewModel.selectedSpecies.collectAsStateWithLifecycle()
    val trip by viewModel.selectedTrip.collectAsStateWithLifecycle()

    val isLoading = (filter.bodyOfWaterId != null && bodyOfWater == null) ||
            (filter.eventId != null && event == null) ||
            (filter.fishermanId != null && fisherman == null) ||
            (filter.lureId != null && lure == null) ||
            (filter.speciesId != null && species == null) ||
            (filter.tripId != null && trip == null)

    val displayItems = if (isLoading) {
        emptyList() // Keep it clean while fetching
    } else {
        listOfNotNull(
            trip?.let { Triple(it.name, AppIcons.Default.Boat, false) },
            event?.let { Triple(it.name, AppIcons.Default.CanoeEmpty, false) },
            species?.let { Triple(it.name, AppIcons.Default.Species, false) },
            bodyOfWater?.let { Triple(it.name, AppIcons.Default.BodyOfWater, false) },
            fisherman?.let { Triple(it.fullName, AppIcons.Default.Fisherman, false) },
            lure?.let { Triple(it.lure.name, AppIcons.Default.Lure, true) }
        )
    }

    // Load fish for the appropriate scope
    val fishForScope by viewModel.fishForScope.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Reports")
                        val total = fishForScope.size
                        BadgeCount(
                            count = total,
                            badgeColor = MaterialTheme.colorScheme.onPrimary,
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (fishForScope.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("There are no fish with the specified criteria.  Go Fish!")
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    displayItems.forEachIndexed { index, (text, iconVector, isLure) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = getOnCardColor().copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            if (isLure) {
                                lure.let { lure ->
                                    LureCompositionWithColors(
                                        name = lure?.lure?.name ?: "",
                                        lure?.primaryColors ?: emptyList(),
                                        lure?.secondaryColors ?: emptyList(),
                                        lure?.lure?.glows ?: false,
                                        lure?.glowColors ?: emptyList(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        contentColor = getOnCardColor(),
                                        colorBadgeSize = 16.dp
                                    )
                                }
                            } else {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = getOnCardColor()
                                )
                            }
                        }
                    }

                    ReportsList(
                        fishList = fishForScope
                    )
                }
            }
        }
    }
}
