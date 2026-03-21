package com.funjim.fishstory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.viewmodels.MainViewModel

// --- Vico 3.x imports ---
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    val fishList by viewModel.allFish.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopBar(navigateBack)
        }
    ) { padding ->
        if (fishList.isEmpty()) {
            EmptyState(padding)
        } else {
            ReportsList(fishList, padding)
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
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text("No fish logged yet. Log some fish to see reports!")
    }
}

@Composable
private fun ReportsList(fishList: List<FishWithDetails>, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Catches by Species", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            SpeciesBarChart(fishList)
        }

        item {
            Text("Catches by Fisherman", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            FishermanBarChart(fishList)
        }
    }
}

@Composable
fun SpeciesBarChart(fishList: List<FishWithDetails>) {
    val speciesCounts = remember(fishList) {
        fishList.groupBy { it.speciesName }.mapValues { it.value.size }
    }
    val labels = remember(speciesCounts) { speciesCounts.keys.toList() }
    val values = remember(speciesCounts) { speciesCounts.values.map { it.toFloat() } }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, x, _ ->
        labels.getOrElse(x.toInt()) { "" }
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisFormatter
            ),
        ),
        modelProducer,
        modifier = Modifier.height(200.dp),
    )
}

@Composable
fun FishermanBarChart(fishList: List<FishWithDetails>) {
    val fishermanCounts = remember(fishList) {
        fishList.groupBy { it.fishermanName }.mapValues { it.value.size }
    }
    val labels = remember(fishermanCounts) { fishermanCounts.keys.toList() }
    val values = remember(fishermanCounts) { fishermanCounts.values.map { it.toFloat() } }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(values) {
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, x, _ ->
        labels.getOrElse(x.toInt()) { "" }
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisFormatter
            ),
        ),
        modelProducer,
        modifier = Modifier.height(200.dp),
    )
}