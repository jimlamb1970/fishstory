package com.funjim.fishstory.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.TripRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.collections.get
import kotlin.collections.map
import kotlin.collections.sorted

class TripListViewModel(
    private val tripRepo: TripRepository
) : ViewModel() {
    // --- Location Logic ---
    private val _deviceLocation = MutableStateFlow<Location?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

    // TODO - can this be replaced by getCurrentLocation in LocationUtils?
    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    suspend fun getTripCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    fun fetchDeviceLocationOnce(context: Context) {
        if (_deviceLocation.value != null) return

        // 1. Explicitly check if permissions are granted
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // 2. Only launch the coroutine if at least one is granted
        if (hasFineLocation || hasCoarseLocation) {
            viewModelScope.launch {
                try {
                    _deviceLocation.value = getTripCurrentLocation(context)
                } catch (e: SecurityException) {
                    // Handle the case where permission was revoked mid-flight
                    _deviceLocation.value = null
                }
            }
        } else {
            // 3. Optional: Trigger a UI event to ask the user for permission
            println("Location permission not granted")
        }
    }

    val uiState: StateFlow<TripUiState> = combine(
        tripRepo.getActiveTripSummaries(),
        tripRepo.getUpcomingTripSummaries(),
        tripRepo.getPreviousTripSummaries()
    ) { active, upcoming, previous ->
        TripUiState(
            liveTrips = active,
            upcomingTrips = upcoming,
            recentTrips = previous,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripUiState(isLoading = true)
    )

    // --- Data Streams ---
    private val allTripSummaries = tripRepo.allTripSummaries
    // TODO -- add sorting on Trip summaries
    val tripSummaries: StateFlow<List<TripSummary>> = allTripSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Actions ---
    fun saveTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepo.upsertTrip(trip)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepo.deleteTripById(trip.id)
        }
    }
}

data class TripListUiState(
    val liveTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<TripSummary> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

class TripListViewModelFactory(
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripListViewModel(tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
