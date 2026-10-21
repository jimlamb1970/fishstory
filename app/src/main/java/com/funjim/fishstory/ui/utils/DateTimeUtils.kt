package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.funjim.fishstory.ui.screens.toUtcMidnight
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    // Converts Long to LocalDateTime for easy picking
    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    // Updates only the Date part of a timestamp
    fun updateDate(currentTimestamp: Long, selectedUtcMidnight: Long): Long {
        val selectedLocalDate = Instant.ofEpochMilli(selectedUtcMidnight)
            .atZone(ZoneOffset.UTC)   // interpret as UTC, not local
            .toLocalDate()

        return Instant.ofEpochMilli(currentTimestamp)
            .atZone(ZoneId.systemDefault())
            .with(selectedLocalDate)  // swap just the date, keep the time
            .toInstant()
            .toEpochMilli()
    }

    // Updates only the Time part of a timestamp
    fun updateTime(currentTimestamp: Long, hour: Int, minute: Int): Long {
        val currentDate = currentTimestamp.toLocalDateTime().toLocalDate()

        return currentDate.atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerButton(
    label: String,
    millis: Long,
    modifier: Modifier = Modifier,
    startTime: Long? = null,
    endTime: Long? = null,
    onConfirm: (Long) -> Unit
) {
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
    }
    var showDateStep by remember { mutableStateOf(false) }
    var showTimeStep by remember { mutableStateOf(false) }
    var pendingMillis by remember { mutableLongStateOf(millis) }

    OutlinedButton(onClick = { showDateStep = true }, modifier = modifier) {
        Text(dateTimeFormatter.format(Date(millis)))
    }

    if (showDateStep) {
        val localCal = Calendar.getInstance()
        localCal.timeInMillis = millis
        val startOfDayUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val datePickerState = key(showDateStep) {
            rememberDatePickerState(
                initialSelectedDateMillis = startOfDayUtc,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val startMidnight = startTime?.toUtcMidnight()
                        val endMidnight = endTime?.toUtcMidnight()

                        val afterStart = startMidnight == null || utcTimeMillis >= startMidnight
                        val beforeEnd = endMidnight == null || utcTimeMillis <= endMidnight

                        return afterStart && beforeEnd
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        val startYear = startTime?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).year
                        }
                        val endYear = endTime?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).year
                        }

                        val afterStart = startYear == null || year >= startYear
                        val beforeEnd = endYear == null || year <= endYear

                        return afterStart && beforeEnd
                    }
                }
            )
        }

        DatePickerDialog(
            onDismissRequest = { showDateStep = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newDate ->
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = newDate
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        cal.set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH))
                        pendingMillis = cal.timeInMillis
                    }
                    showDateStep = false
                    showTimeStep = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDateStep = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimeStep) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = pendingMillis
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimeStep = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select $label time",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimeStep = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            onConfirm(cal.timeInMillis)
                            showTimeStep = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

enum class LabelPosition {
    Top,
    Start
}

@Composable
fun DurationStepperField(
    label: String,
    totalMillis: Long,
    onMillisChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
    maxMillis: Long? = null,
    labelAlignment: TextAlign = TextAlign.Start,
    labelPosition: LabelPosition = LabelPosition.Top
) {
    val totalHours = TimeUnit.MILLISECONDS.toHours(totalMillis).coerceAtLeast(0)
    val days = (totalHours / 24).toInt()
    val hours = (totalHours % 24).toInt()

    val maxTotalHours = maxMillis?.let { TimeUnit.MILLISECONDS.toHours(it).coerceAtLeast(0) }
    val maxDays = maxTotalHours?.div(24)?.toInt()

    val maxRemainingHours = if (maxTotalHours != null && maxDays != null) {
        maxTotalHours - (maxDays * 24)
    } else {
        null
    }

    // Shared steppers content
    @Composable
    fun StepperRows(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- DAYS STEPPER ---
            StepperField(
                label = "Days",
                value = days.toString(),
                onValueChange = { input ->
                    val newDays = input.toIntOrNull() ?: 0
                    val cappedDays = maxDays?.let { newDays.coerceAtMost(it) } ?: newDays
                    val finalDays = cappedDays

                    val finalHours = if (finalDays == maxDays && maxRemainingHours != null) {
                        hours.coerceAtMost(maxRemainingHours.toInt().coerceAtLeast(0))
                    } else {
                        hours
                    }

                    val newTotalMillis = TimeUnit.DAYS.toMillis(finalDays.toLong()) + TimeUnit.HOURS.toMillis(finalHours.toLong())
                    onMillisChanged(newTotalMillis.coerceAtMost(maxMillis ?: newTotalMillis))
                },
                onIncrement = {
                    val nextDays = days + 1
                    val newTotalMillis = TimeUnit.DAYS.toMillis(nextDays.toLong()) + TimeUnit.HOURS.toMillis(hours.toLong())
                    if (maxMillis == null || newTotalMillis <= maxMillis) {
                        onMillisChanged(newTotalMillis)
                    }
                },
                onDecrement = {
                    if (days > 0) {
                        val nextDays = days - 1
                        val newTotalMillis = TimeUnit.DAYS.toMillis(nextDays.toLong()) + TimeUnit.HOURS.toMillis(hours.toLong())
                        onMillisChanged(newTotalMillis)
                    }
                },
                modifier = Modifier.weight(1f),
                minValue = 0,
                maxValue = maxDays,
                buttonSize = 32.dp,
                labelAlignment = TextAlign.Center
            )

            // --- HOURS STEPPER ---
            StepperField(
                label = "Hours",
                value = hours.toString(),
                onValueChange = { input ->
                    val rawHours = input.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val additionalDays = rawHours / 24
                    val remainingHours = rawHours % 24

                    val targetDays = days + additionalDays
                    val targetHours = remainingHours

                    val newTotalMillis = TimeUnit.DAYS.toMillis(targetDays.toLong()) + TimeUnit.HOURS.toMillis(targetHours.toLong())
                    val finalMillis = maxMillis?.let { newTotalMillis.coerceAtMost(it) } ?: newTotalMillis

                    onMillisChanged(finalMillis)
                },
                onIncrement = {
                    var nextHours = hours + 1
                    var nextDays = days

                    if (nextHours >= 24) {
                        nextHours = 0
                        nextDays += 1
                    }

                    val newTotalMillis = TimeUnit.DAYS.toMillis(nextDays.toLong()) + TimeUnit.HOURS.toMillis(nextHours.toLong())
                    if (maxMillis == null || newTotalMillis <= maxMillis) {
                        onMillisChanged(newTotalMillis)
                    }
                },
                onDecrement = {
                    var nextHours = hours - 1
                    var nextDays = days

                    if (nextHours < 0) {
                        if (nextDays > 0) {
                            nextDays -= 1
                            nextHours = 23
                        } else {
                            nextHours = 0
                        }
                    }

                    val newTotalMillis = TimeUnit.DAYS.toMillis(nextDays.toLong()) + TimeUnit.HOURS.toMillis(nextHours.toLong())
                    onMillisChanged(newTotalMillis)
                },
                modifier = Modifier.weight(1f),
                minValue = 0,
                maxValue = if (maxDays != null && days == maxDays) {
                    maxRemainingHours?.toInt()?.coerceAtLeast(0)
                } else {
                    23
                },
                buttonSize = 32.dp,
                labelAlignment = TextAlign.Center
            )
        }
    }

    if (labelPosition == LabelPosition.Top) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = labelAlignment,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            StepperRows(modifier = Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.width(48.dp) // Matches your DateTimeButton label widths
                )
            }
            StepperRows(modifier = Modifier.weight(1f))
        }
    }
}