package com.funjim.fishstory

//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.FishWithDetails
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

import java.time.ZoneId

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsList(fishList: List<FishWithDetails>, padding: PaddingValues) {
    var selectedReport by remember { mutableStateOf(ReportType.SPECIES) }
    var expanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedReport.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Report Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ReportType.entries.forEach { report ->
                        DropdownMenuItem(
                            text = { Text(report.label) },
                            onClick = {
                                selectedReport = report
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(selectedReport.label, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            when (selectedReport) {
                ReportType.SPECIES -> SpeciesBarChart(fishList)
                ReportType.FISHERMAN -> FishermanBarChart(fishList)
                ReportType.FISHERMAN_PIE -> FishermanPieChart(fishList)
                ReportType.FISHERMAN_SPECIES -> FishermanStackedBarChart(fishList)
                ReportType.CATCHES_BY_HOUR -> CatchesByHourLineChart(fishList)
                ReportType.CATCHES_BY_SIZE -> CatchesBySizeBarChart(fishList)
            }
        }
    }
}

@Composable
fun SpeciesBarChart(fishList: List<FishWithDetails>) {
    val speciesCounts = remember(fishList) {
        fishList.groupingBy { it.speciesName }.eachCount()
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
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomAxisFormatter),
        ),
        modelProducer,
        modifier = Modifier.height(200.dp),
    )
}

@Composable
fun FishermanBarChart(fishList: List<FishWithDetails>) {
    val fishermanCounts = remember(fishList) {
        fishList.groupingBy { it.fishermanName }.eachCount()
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
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomAxisFormatter),
        ),
        modelProducer,
        modifier = Modifier.height(200.dp),
    )
}

@Composable
fun FishermanPieChart(fishList: List<FishWithDetails>) {
    val fishermanCounts = remember(fishList) {
        fishList.groupingBy { it.fishermanName }.eachCount()
            .entries
            .sortedByDescending { it.value }
    }

    val total = remember(fishermanCounts) {
        fishermanCounts.sumOf { it.value }.toFloat()
    }

    if (total == 0f) {
        Box(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("No catch data recorded.")
        }
        return
    }

    val colors = SPECIES_COLORS // reuse the same palette already defined in your file

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val diameter = min(size.width, size.height) * 0.85f
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f // start at 12 o'clock

            fishermanCounts.forEachIndexed { index, (_, count) ->
                val sweepAngle = (count / total) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
                // Draw a thin white divider between slices
                drawArc(
                    color = androidx.compose.ui.graphics.Color.White,
                    startAngle = startAngle,
                    sweepAngle = 1f,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
                startAngle += sweepAngle
            }
        }

        // Legend
        Spacer(modifier = Modifier.height(12.dp))
        fishermanCounts.forEachIndexed { index, (name, count) ->
            val percentage = ((count / total) * 100).toInt()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = colors[index % colors.size]
                ) {}
                Text(
                    "$name — $count catch${if (count != 1) "es" else ""} ($percentage%)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// A stable palette — extend if you have more species than colors here
private val SPECIES_COLORS = listOf(
    Color(0xFF4E79A7),
    Color(0xFFF28E2B),
    Color(0xFFE15759),
    Color(0xFF76B7B2),
    Color(0xFF59A14F),
    Color(0xFFEDC948),
    Color(0xFFB07AA1),
    Color(0xFFFF9DA7),
)

@Composable
fun FishermanStackedBarChart(fishList: List<FishWithDetails>) {
    // All unique species, sorted for a stable legend order
    val allSpecies = remember(fishList) {
        fishList.map { it.speciesName }.distinct().sorted()
    }

    // All unique fishermen, sorted for stable x-axis order
    val allFishermen = remember(fishList) {
        fishList.map { it.fishermanName }.distinct().sorted()
    }

    // For each species (one series per species), build a list of counts
    // indexed by fisherman position. Missing combinations = 0.
    val seriesData: List<List<Float>> = remember(fishList, allSpecies, allFishermen) {
        val countMap = fishList.groupBy { it.fishermanName }
            .mapValues { (_, catches) ->
                catches.groupingBy { it.speciesName }.eachCount()
            }
        allSpecies.map { species ->
            allFishermen.map { fisherman ->
                (countMap[fisherman]?.get(species) ?: 0).toFloat()
            }
        }
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(seriesData) {
        modelProducer.runTransaction {
            columnSeries {
                seriesData.forEach { counts -> series(counts) }
            }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, x, _ ->
        allFishermen.getOrElse(x.toInt()) { "" }
    }

    // One colored column component per species
    val columnComponents = allSpecies.mapIndexed { index, _ ->
        rememberLineComponent(
            fill = Fill(SPECIES_COLORS[index % SPECIES_COLORS.size]),
            thickness = 16.dp,
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(columnComponents),
                mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisFormatter
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.height(250.dp),
    )

    // Legend
    Spacer(modifier = Modifier.height(12.dp))
    allSpecies.forEachIndexed { index, species ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = SPECIES_COLORS[index % SPECIES_COLORS.size]
            ) {}
            Text(species, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CatchesByHourLineChart(fishList: List<FishWithDetails>) {
    // Count catches per hour of day (0–23), defaulting missing hours to 0
    val hourlyCounts: List<Float> = remember(fishList) {
        val counts = IntArray(24)
        fishList.forEach { fish ->
            // Assumes FishWithDetails has a timestamp (Long epoch ms) field called `caughtAt`
            val hour = java.time.Instant
                .ofEpochMilli(fish.timestamp)
                .atZone(ZoneId.systemDefault())
                .hour
            counts[hour]++
        }
        counts.map { it.toFloat() }
    }

    // Trim leading and trailing hours with no catches
    val trimmedData: Pair<Int, List<Float>> = remember(hourlyCounts) {
        val firstNonZero = hourlyCounts.indexOfFirst { it > 0 }
        val lastNonZero = hourlyCounts.indexOfLast { it > 0 }
        if (firstNonZero == -1) {
            Pair(0, emptyList()) // no catches at all
        } else {
            val start = (firstNonZero - 1).coerceAtLeast(0)
            val end = (lastNonZero + 1).coerceAtMost(23)
            Pair(start, hourlyCounts.subList(start, end + 1))
        }
    }

    val startHour = trimmedData.first
    val trimmedCounts = trimmedData.second

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(trimmedCounts) {
        if (trimmedCounts.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(trimmedCounts) }
            }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, x, _ ->
        val hour = (startHour + x.toInt()).coerceIn(0, 23)
        when {
            hour == 0 -> "12a"
            hour < 12 -> "${hour}a"
            hour == 12 -> "12p"
            else -> "${hour - 12}p"
        }
    }

    val line = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary))
    )

    if (trimmedCounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No catch times recorded.")
        }
    } else {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(line)
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = bottomAxisFormatter,
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.height(250.dp),
        )
    }
}

@Composable
fun CatchesBySizeBarChart(fishList: List<FishWithDetails>) {
    val sizeBuckets = listOf(
        "≤8\"",
        "8-10\"",
        "10-12\"",
        "12-14\"",
        "14-16\"",
        "16-18\"",
        "18-20\"",
        ">20\""
    )

    val bucketCounts: List<Float> = remember(fishList) {
        val counts = IntArray(sizeBuckets.size)
        fishList.forEach { fish ->
            val bucket = when {
                fish.length <= 8 -> 0
                fish.length <= 10 -> 1
                fish.length <= 12 -> 2
                fish.length <= 14 -> 3
                fish.length <= 16 -> 4
                fish.length <= 18 -> 5
                fish.length <= 20 -> 6
                else              -> 7
            }
            counts[bucket]++
        }
        counts.map { it.toFloat() }
    }

    // Trim empty buckets from each end, keeping one zero buffer
    val trimmedData: Pair<Int, List<Float>> = remember(bucketCounts) {
        val firstNonZero = bucketCounts.indexOfFirst { it > 0 }
        val lastNonZero = bucketCounts.indexOfLast { it > 0 }
        if (firstNonZero == -1) {
            Pair(0, emptyList())
        } else {
            val start = (firstNonZero - 1).coerceAtLeast(0)
            val end = (lastNonZero + 1).coerceAtMost(sizeBuckets.lastIndex)
            Pair(start, bucketCounts.subList(start, end + 1))
        }
    }

    val startIndex = trimmedData.first
    val trimmedCounts = trimmedData.second

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(trimmedCounts) {
        if (trimmedCounts.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(trimmedCounts) }
            }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { _, x, _ ->
        sizeBuckets.getOrElse(startIndex + x.toInt()) { "" }
    }

    if (trimmedCounts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("No size data recorded.")
        }
    } else {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = bottomAxisFormatter,
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.height(250.dp),
        )
    }
}