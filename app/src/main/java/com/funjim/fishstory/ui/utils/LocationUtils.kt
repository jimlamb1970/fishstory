package com.funjim.fishstory.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng

class LocationUtils

fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun rememberLocationPickerState(
    deviceLocation: Pair<Double, Double>?,
    existingLat: Double?,
    existingLng: Double?,
    onFetchLocation: () -> Unit,
    onLocationConfirmed: (Double, Double) -> Unit
): LocationPickerResult {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    // Logic: Determine the "best" initial point
    // We use 'derivedStateOf' so this recalculates correctly if deviceLocation arrives late
    val initialLatLng = remember(deviceLocation, existingLat, existingLng) {
        when {
            // 1. Priority: If we already have a saved location for this trip/segment
            existingLat != null && existingLng != null -> LatLng(existingLat, existingLng)
            // 2. Fallback: Use the device's current location fetched by VM
            deviceLocation != null -> LatLng(deviceLocation.first, deviceLocation.second)
            // 3. Last Resort: Default center (or null to show a loading state)
            else -> LatLng(45.0, -90.0)
        }
    }

    // Trigger the "Once" fetch when the screen starts
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            onFetchLocation()
        }
    }

    // UI Logic: Show the dialog if the flag is true
    if (showDialog) {
        MapPickerSelectionDialog(
            initialLatLng = initialLatLng,
            onDismiss = { showDialog = false },
            onConfirm = { latLng ->
                scope.launch {
                    onLocationConfirmed(latLng.latitude, latLng.longitude)
                }
                showDialog = false
            }
        )
    }

    return remember {
        LocationPickerResult(openPicker = { showDialog = true })
    }
}

class LocationPickerResult(val openPicker: () -> Unit)
{
}

@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return try {
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        location?.let { it.latitude to it.longitude }
    } catch (e: Exception) {
        null
    }
}
