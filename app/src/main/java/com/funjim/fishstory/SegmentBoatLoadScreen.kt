package com.funjim.fishstory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentBoatLoadScreen(
    viewModel: MainViewModel,
    segmentId: String,
    tripId: String,
    navigateBack: () -> Unit
) {
    val tripWithDetails by viewModel.getTripWithDetails(tripId).collectAsState(initial = null)
    val segmentWithFishermen by viewModel.getSegmentWithFishermen(segmentId).collectAsState(initial = null)
    
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Load Boat", style = MaterialTheme.typography.titleLarge)
                        segmentWithFishermen?.segment?.name?.let { 
                            Text(it, style = MaterialTheme.typography.bodySmall) 
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select from fishermen on this trip:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            tripWithDetails?.let { tripDetails ->
                val currentFishermanIds = segmentWithFishermen?.fishermen?.map { it.id }?.toSet() ?: emptySet()

                if (tripDetails.fishermen.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No fishermen found for this trip.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tripDetails.fishermen) { fisherman ->
                            val isSelected = currentFishermanIds.contains(fisherman.id)
                            
                            FishermanSelectionRow(
                                fisherman = fisherman,
                                isSelected = isSelected,
                                onToggle = {
                                    scope.launch {
                                        if (isSelected) {
                                            viewModel.deleteFishermanFromSegment(segmentId, fisherman.id)
                                        } else {
                                            viewModel.addFishermanToSegment(segmentId, fisherman.id)
                                        }
                                    }
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun FishermanSelectionRow(
    fisherman: Fisherman,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${fisherman.firstName} ${fisherman.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (fisherman.nickname.isNotEmpty()) {
                Text(
                    text = fisherman.nickname,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
