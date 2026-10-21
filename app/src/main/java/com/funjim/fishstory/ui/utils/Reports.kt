package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.ui.screens.ReportType
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
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.Instant
import java.time.ZoneId
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsList(
    fishList: List<FishWithDetails>
) {
    var reportExpanded by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf(ReportType.SPECIES) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Report type dropdown
        item {
            ExposedDropdownMenuBox(
                expanded = reportExpanded,
                onExpandedChange = { reportExpanded = !reportExpanded }
            ) {
                OutlinedTextField(
                    value = selectedReport.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Report Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reportExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = reportExpanded,
                    onDismissRequest = { reportExpanded = false }
                ) {
                    ReportType.entries.forEach { report ->
                        DropdownMenuItem(
                            text = { Text(report.label) },
                            onClick = {
                                selectedReport = report
                                reportExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Chart
        item {
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
        fishList.groupingBy { it.species?.name }.eachCount()
    }

    val labels = remember(speciesCounts) { speciesCounts.keys.toList() }
    val values = remember(speciesCounts) { speciesCounts.values.map { it.toFloat() } }

    val modelProducer = remember { CartesianChartModelProducer() }

    var isSyncing by remember(fishList) { mutableStateOf(true) }

    LaunchedEffect(values) {
        isSyncing = true
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
        isSyncing = false
    }

    val axisLabelComponent = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
    )

    val bottomAxisFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ ->
            labels.getOrNull(x.toInt()) ?: ""
        }
    }

    if (isSyncing) {

    } else {
        CartesianChartHost(
            rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(
                    label = axisLabelComponent
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = axisLabelComponent,
                    valueFormatter = bottomAxisFormatter),
            ),
            modelProducer,
            modifier = Modifier.height(200.dp),
        )
    }
}

@Composable
fun FishermanBarChart(
    fishList: List<FishWithDetails>
) {
    val fishermanCounts = remember(fishList) {
        fishList.groupingBy { it.fisherman?.fullName }.eachCount()
    }
    val labels = remember(fishermanCounts) { fishermanCounts.keys.toList() }
    val values = remember(fishermanCounts) { fishermanCounts.values.map { it.toFloat() } }

    val modelProducer = remember { CartesianChartModelProducer() }

    var isSyncing by remember(fishList) { mutableStateOf(true) }

    LaunchedEffect(values) {
        isSyncing = true
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
        isSyncing = false
    }

    val axisLabelComponent = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
    )

    val bottomAxisFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ ->
            labels.getOrNull(x.roundToInt()) ?: " "
        }
    }

    if (isSyncing) {
        Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        key(labels.size) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = VerticalAxis.rememberStart(
                        label = axisLabelComponent),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = axisLabelComponent,
                        valueFormatter = bottomAxisFormatter),
                ),
                modelProducer = modelProducer,
                modifier = Modifier.height(200.dp),
            )
        }
    }
}

@Composable
fun FishermanPieChart(fishList: List<FishWithDetails>) {
    val fishermanCounts = remember(fishList) {
        fishList.groupingBy { it.fisherman?.fullName }.eachCount()
            .entries
            .sortedByDescending { it.value }
    }

    val total = remember(fishermanCounts) {
        fishermanCounts.sumOf { it.value }.toFloat()
    }

    if (total == 0f) {
        Box(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = Alignment.Center
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
                    color = Color.White,
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
                verticalAlignment = Alignment.CenterVertically,
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
        fishList.map { it.species?.name ?: "" }.distinct().sorted()
    }

    // All unique fishermen, sorted for stable x-axis order
    val allFishermen = remember(fishList) {
        fishList.map { it.fisherman?.fullName ?: "" }.distinct().sorted()
    }

    // For each species (one series per species), build a list of counts
    // indexed by fisherman position. Missing combinations = 0.
    val seriesData: List<List<Float>> = remember(fishList, allSpecies, allFishermen) {
        val countMap = fishList.groupBy { it.fisherman?.fullName }
            .mapValues { (_, catches) ->
                catches.groupingBy { it.species?.name }.eachCount()
            }
        allSpecies.map { species ->
            allFishermen.map { fisherman ->
                (countMap[fisherman]?.get(species) ?: 0).toFloat()
            }
        }
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    var isSyncing by remember(fishList) { mutableStateOf(true) }

    LaunchedEffect(seriesData) {
        isSyncing = true
        modelProducer.runTransaction {
            columnSeries {
                seriesData.forEach { counts -> series(counts) }
            }
        }
        isSyncing = false
    }

    val axisLabelComponent = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
    )

    val bottomAxisFormatter = remember(allFishermen) {
        CartesianValueFormatter { _, x, _ ->
            allFishermen.getOrNull(x.toInt()) ?: ""
        }
    }

    // One colored column component per species
    val columnComponents = allSpecies.mapIndexed { index, _ ->
        rememberLineComponent(
            fill = Fill(SPECIES_COLORS[index % SPECIES_COLORS.size]),
            thickness = 16.dp,
        )
    }

    if (isSyncing) {
        Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(columnComponents),
                    mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = axisLabelComponent
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = axisLabelComponent,
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
                verticalAlignment = Alignment.CenterVertically,
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
}

@Composable
fun CatchesByHourLineChart(fishList: List<FishWithDetails>) {
    // Count catches per hour of day (0–23), defaulting missing hours to 0
    val hourlyCounts: List<Float> = remember(fishList) {
        val counts = IntArray(24)
        fishList.forEach { fish ->
            // Assumes FishWithDetails has a timestamp (Long epoch ms) field called `caughtAt`
            val hour = Instant
                .ofEpochMilli(fish.fish.timestamp)
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

    var isSyncing by remember(fishList) { mutableStateOf(true) }

    LaunchedEffect(trimmedCounts) {
        isSyncing = true
        if (trimmedCounts.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(trimmedCounts) }
            }
        }
        isSyncing = false
    }

    val axisLabelComponent = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
    )

    val bottomAxisFormatter = remember(startHour) {
        CartesianValueFormatter { _, x, _ ->
            val hour = (startHour + x.toInt())
            if (hour in 0..23) {
                when {
                    hour == 0 -> "12a"
                    hour < 12 -> "${hour}a"
                    hour == 12 -> "12p"
                    else -> "${hour - 12}p"
                }
            } else ""
        }
    }

    val line = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(MaterialTheme.colorScheme.primary))
    )

    if (trimmedCounts.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = Alignment.Center) {
            Text("No catch times recorded.")
        }
    } else if (isSyncing) {
        Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(line)
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = axisLabelComponent
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = axisLabelComponent,
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
                fish.fish.length?.toInches()!! <= 8 -> 0
                fish.fish.length.toInches() <= 10 -> 1
                fish.fish.length.toInches() <= 12 -> 2
                fish.fish.length.toInches() <= 14 -> 3
                fish.fish.length.toInches() <= 16 -> 4
                fish.fish.length.toInches() <= 18 -> 5
                fish.fish.length.toInches() <= 20 -> 6
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

    var isSyncing by remember(fishList) { mutableStateOf(true) }

    LaunchedEffect(trimmedCounts) {
        isSyncing = true
        if (trimmedCounts.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(trimmedCounts) }
            }
        }
        isSyncing = false
    }

    val axisLabelComponent = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
    )

    val bottomAxisFormatter = remember(sizeBuckets) {
        CartesianValueFormatter { _, x, _ ->
            sizeBuckets.getOrNull(startIndex + x.toInt()) ?: ""
        }
    }

    if (trimmedCounts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No size data recorded.")
        }
    } else if (isSyncing) {
        Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(
                    label = axisLabelComponent
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = axisLabelComponent,
                    valueFormatter = bottomAxisFormatter,
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.height(250.dp),
        )
    }
}