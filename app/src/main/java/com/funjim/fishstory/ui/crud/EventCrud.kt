package com.funjim.fishstory.ui.crud

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.utils.DateTimePickerButton

@Composable
fun EditEventDialog(
    event: Event,
    trip: Trip,
    onConfirm: (Event) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var origName by remember { mutableStateOf(event.name) }
    var origStart by remember { mutableLongStateOf(event.startTime) }
    var origEnd by remember { mutableLongStateOf(event.endTime) }
    var tripStart by remember { mutableLongStateOf(trip.startDate) }
    var tripEnd by remember { mutableLongStateOf(trip.endDate) }

    var name by remember { mutableStateOf(origName) }
    var startTime by remember { mutableStateOf(origStart) }
    var endTime by remember { mutableStateOf(origEnd) }

    val isChanged = name != origName ||
            startTime != origStart ||
            endTime != origEnd

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Event") },
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
                    startTime = tripStart,
                    endTime = tripEnd,
                    modifier = Modifier.fillMaxWidth()
                ) { newTime ->
                    if (newTime < tripStart) {
                        Toast.makeText(
                            context,
                            "Event start time cannot be before trip start time.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (newTime > tripEnd) {
                        Toast.makeText(
                            context,
                            "Event start time cannot be after trip end time.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        startTime = newTime
                        if (startTime > endTime)
                            endTime = startTime
                    }
                }

                Text("End", style = MaterialTheme.typography.labelLarge)
                DateTimePickerButton(
                    label = "End",
                    millis = endTime,
                    startTime = tripStart,
                    endTime = tripEnd,
                    modifier = Modifier.fillMaxWidth()
                ) { newTime ->
                    if (newTime < startTime) {
                        Toast.makeText(
                            context,
                            "Event end time cannot be before event start time.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (newTime > tripEnd) {
                        Toast.makeText(
                            context,
                            "Event end time cannot be after trip end time.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        endTime = newTime
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(event.copy(
                        name = name.trim(),
                        startTime = startTime,
                        endTime = endTime)
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