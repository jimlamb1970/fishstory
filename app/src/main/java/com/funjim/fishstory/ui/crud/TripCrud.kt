package com.funjim.fishstory.ui.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.DurationStepperField
import com.funjim.fishstory.ui.utils.LabelPosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditTripDialog(
    item: Trip,
    onConfirm: (Trip) -> Unit,
    onDismiss: () -> Unit
) {
    val origName by remember { mutableStateOf(item.name) }
    val origStart by remember { mutableLongStateOf(item.startDate) }
    val origEnd by remember { mutableLongStateOf(item.endDate) }

    var name by remember { mutableStateOf(origName) }
    var startTime by remember { mutableLongStateOf(origStart) }
    var endTime by remember { mutableLongStateOf(origEnd) }

    val isChanged = name != origName ||
            startTime != origStart ||
            endTime != origEnd

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Start", style = MaterialTheme.typography.labelLarge)
                DateTimePickerButton(
                    label = "Start",
                    millis = startTime,
                    modifier = Modifier.fillMaxWidth()
                ) { newTime ->
                    val currentDuration = (endTime - startTime).coerceAtLeast(0L)
                    startTime = newTime
                    endTime = newTime + currentDuration
                }

                DurationStepperField(
                    label = "Duration",
                    totalMillis = (endTime - startTime).coerceAtLeast(0L),
                    onMillisChanged = { newDurationMillis ->
                        endTime = startTime + newDurationMillis
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxMillis = 1000L * 60 * 60 * 24 * 365,
                    labelPosition = LabelPosition.Top
                )

                val dateTimeFormatter = remember {
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "End:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = dateTimeFormatter.format(Date(endTime)),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(item.copy(
                        name = name.trim(),
                        startDate = startTime,
                        endDate = endTime)
                    )
                },
                enabled = name.isNotBlank() && isChanged
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}