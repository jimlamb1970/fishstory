package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.TripSummary
import kotlinx.coroutines.launch

@Composable
fun EventHighlightCard(
    summary: EventSummary,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fish Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )

            // Top Row: The Numbers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(
                    label = "CAUGHT",
                    value = "${summary.fishCaught}",
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary)
                StatItem(
                    label = "KEPT",
                    value = "${summary.fishKept}",
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Bottom Row: The Achievements
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AchievementItem(
                    icon = Icons.Default.Person,
                    label = "Top Rod",
                    name = summary.mostCaughtName,
                    description = "(${summary.mostCaught} fish)",
                    modifier = Modifier.weight(1f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary)

                AchievementItem(
                    icon = Icons.Default.Star,
                    label = "Big Fish",
                    name = summary.bigFishName,
                    description = "(${summary.bigFishLength?.toDisplayString(
                        useMetric = false,
                        useFractions = true
                    )} : ${summary.bigFishSpecies})",
                    modifier = Modifier.weight(1f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun FishermanHighlightCard(
    stats: FishermanFullStatistics,
    onClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> HighlightsPage(stats)
                    1 -> LowlightsPage(stats)
                }
            }

            // Dot indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { i ->
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ).clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightsPage(stats: FishermanFullStatistics) {
    Column {
        Text(
            text = "HIGHLIGHTS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (stats.largestFishLength != null) {
                StatItem(
                    label = "LARGEST FISH",
                    value = stats.largestFishLength.toDisplayString(
                        useMetric = false,
                        useFractions = true
                    ),
                    description = stats.largestFishSpecies,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (!stats.bestTripName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.Default.Celebration,
                    label = "Best Trip",
                    name = stats.bestTripName,
                    description = "(${stats.mostTripCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!stats.bestEventName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Best Event",
                    name = "${stats.bestEventName} - (${stats.bestEventTripName})",
                    description = "(${stats.mostEventCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LowlightsPage(stats: FishermanFullStatistics) {
    Column {
        Text(
            text = "LOWLIGHTS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (stats.smallestFishLength != null) {
                StatItem(
                    label = "SMALLEST FISH",
                    value = stats.smallestFishLength.toDisplayString(
                        useMetric = false,
                        useFractions = true
                    ),
                    description = stats.smallestFishSpecies,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!stats.worstTripName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.Default.Warning,
                    label = "Worst Trip",
                    name = stats.worstTripName,
                    description = "(${stats.fewestTripCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!stats.worstEventName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "Worst Event",
                    name = "${stats.worstEventName} - (${stats.worstEventTripName})",
                    description = "(${stats.fewestEventCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
fun TripHighlightCard(
    tripSummary: TripSummary,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fish Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )

            // Top Row: The Numbers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(
                    label = "CAUGHT",
                    value = "${tripSummary.fishCaught}",
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary)

                StatItem(
                    label = "KEPT",
                    value = "${tripSummary.fishKept}",
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Bottom Row: The Achievements
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AchievementItem(
                    icon = Icons.Default.Person,
                    label = "Top Rod",
                    name = tripSummary.mostCaughtName,
                    description = "(${tripSummary.mostCaught} fish)",
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))

                AchievementItem(
                    icon = Icons.Default.Star,
                    label = "Big Fish",
                    name = tripSummary.bigFishName,
                    description = "(${
                        tripSummary.bigFishLength?.toDisplayString(
                            useMetric = false,
                            useFractions = true
                        )
                    } : ${tripSummary.bigFishSpecies})",
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}
