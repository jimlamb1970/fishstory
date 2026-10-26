package com.funjim.fishstory.ui.selection

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitSummary
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.cards.formatSizeConstraint
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.GumballStatus
import com.funjim.fishstory.ui.utils.ModalAddButton
import com.funjim.fishstory.ui.utils.StatusGumball
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.getOnChipColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitSelection(
    items: List<Limit>,
    selectedItems: List<LimitSummary>,
    useImperial: Boolean,
    speciesThumbnailProvider: @Composable ((Species) -> Unit),
    onSelected: (Limit) -> Unit,
    onUnselected: (Limit) -> Unit,
    onAdd: (() -> Unit)? = null,
    onDone: () -> Unit,
    usageMap: Map<String, Int>? = null,
    maxUsage: Int? = null
) {
    var showSheet by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                onDone()
            },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Limits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                        onDone()
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxHeight(0.8f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it.trimStart() },
                    label = { Text("Search limits ...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = remember(searchQuery, items) {
                    items.filter { item ->
                        item.name.contains(searchQuery, ignoreCase = true) ||
                                item.species.any { it.name.contains(searchQuery, ignoreCase = true) }
                    }.sortedBy { it.name }
                }
                val filteredSize = filtered.size

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    listItemsIndexed(
                        items = filtered,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        val isChecked = selectedItems.any { it.limit.id == item.id }

                        val state =
                            if (isChecked) {
                                val usage = usageMap?.get(item.id) ?: 0
                                if (maxUsage != null && usage < maxUsage) {
                                    ToggleableState.Indeterminate
                                }
                                else ToggleableState.On
                            } else ToggleableState.Off

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .border(
                                    width = if (isChecked) 2.dp else 0.dp,
                                    color =
                                        when (state) {
                                            ToggleableState.On ->
                                                getOnCardColor()
                                            ToggleableState.Indeterminate ->
                                                getOnCardColor().copy(alpha = 0.5f)
                                            else ->
                                                Color.Transparent
                                        },
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clickable(enabled = true) {
                                    if (state == ToggleableState.On) onUnselected(item)
                                    else onSelected(item)
                                },
                            leadingContent = {
                                StatusGumball(
                                    status = GumballStatus.GOOD,
                                    size = 32.dp,
                                    borderWidth = 2.dp,
                                    borderColor = getOnCardSecondaryColor()
                                )
                            },
                            headlineContent = {
                                Column() {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.type.label,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    if (isChecked && maxUsage != null && maxUsage > 0) {
                                        val usage = usageMap?.get(item.id) ?: 0
                                        CardItemWithValue(
                                            icon = AppIcons.Default.CanoeEmpty,
                                            value = "$usage",
                                            description = "Events",
                                            contentColor = getOnCardColor()
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Base Limit: ${item.count}") },
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }
                                    val sizeDetails = remember(item) {
                                        formatSizeConstraint(item, useImperial = useImperial)
                                    }
                                    if (sizeDetails.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AssistChip(
                                                onClick = { },
                                                label = { Text(sizeDetails) },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                    }

                                    if (item.species.isNotEmpty()) {
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val species = item.species.sortedBy { it.name }
                                            items(species) { species ->
                                                InputChip(
                                                    selected = true,
                                                    onClick = {},
                                                    label = { Text(species.name) },
                                                    avatar = { speciesThumbnailProvider(species) },
                                                    colors = InputChipDefaults.inputChipColors(
                                                        selectedContainerColor = getCardColor().copy(alpha = 0.15f),
                                                        selectedLabelColor = getOnCardColor(),
                                                        selectedLeadingIconColor = getOnCardColor(),
                                                        selectedTrailingIconColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        enabled = true,
                                                        selected = true,
                                                        selectedBorderColor = getCardBorderColor(),
                                                        selectedBorderWidth = 1.dp,
                                                        borderColor = getOnChipColor(),
                                                        borderWidth = 1.dp
                                                    ),
                                                    trailingIcon = {
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            },
                            trailingContent = {
                                TriStateCheckbox(
                                    state = state,
                                    onClick = null,
                                    enabled = true
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = getCardColor(index, filteredSize, isChecked),
                                headlineColor = getOnCardColor()
                            )
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    if (onAdd != null) {
                        item {
                            ModalAddButton(
                                title = "Add limit ...",
                                onAdd = { onAdd() }
                            )
                        }
                    }
                }
            }
        }
    }
}