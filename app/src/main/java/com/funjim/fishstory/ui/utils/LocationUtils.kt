package com.funjim.fishstory.ui.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.funjim.fishstory.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng

class LocationUtils

interface LocationProvider {
    val deviceLocation: StateFlow<Location?>
    suspend fun fetchLocation(): Location?
    suspend fun fetchDeviceLocationOnce(): Location?

    fun hasLocationPermission(): Boolean
}

class LocationProviderImpl(
    private val repository: LocationRepository
) : LocationProvider {
    private val _deviceLocation = MutableStateFlow<Location?>(null)
    override val deviceLocation = _deviceLocation.asStateFlow()

    override suspend fun fetchLocation(): Location? {
        if (repository.hasLocationPermission()) {
            return repository.getDeviceLocation()
        }
        return null
    }

    override suspend fun fetchDeviceLocationOnce(): Location? {
        if (_deviceLocation.value != null) return deviceLocation.value

        if (repository.hasLocationPermission()) {
            _deviceLocation.value = repository.getDeviceLocation()
            return deviceLocation.value
        }
        return null
    }

    override fun hasLocationPermission(): Boolean {
        return repository.hasLocationPermission()
    }
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
