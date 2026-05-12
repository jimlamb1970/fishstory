package com.funjim.fishstory.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.getCurrentLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: TripRepository
) : ViewModel() {
    private val currentTime = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    private val _deviceLocation = MutableStateFlow<Location?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()
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
                    _deviceLocation.value = getCurrentLocation(context)
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

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getActiveTripSummaries(),
        repository.getUpcomingTrips(),
        repository.getPreviousTripSummaries()
    ) { active, upcoming, previous ->
        DashboardUiState(
            activeTrips = active,
            upcomingTrips = upcoming,
            recentTrips = previous.take(5), // Only show the last 5 on the dashboard
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTripEvents: StateFlow<SegmentGroups> = currentTime
        .flatMapLatest { now ->
            repository.getSegmentsForActiveTrips(now).map { allSegments ->
                // Split them into the 3 groups here
                SegmentGroups(
                    previous = allSegments.filter { it.event.endTime < now },
                    active = allSegments.filter { now in it.event.startTime..it.event.endTime },
                    upcoming = allSegments.filter { it.event.startTime > now }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SegmentGroups()
        )

    private val _activeSegmentIndex = MutableStateFlow(0)
    val activeSegmentIndex: StateFlow<Int> = _activeSegmentIndex

    private val _activeTripId = MutableStateFlow<String?>(null)
    fun setActiveTripId(id: String) {
        _activeTripId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentSummaries: StateFlow<List<EventSummary>> = _activeTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                repository.getSegmentSummaries(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeSegments: StateFlow<List<Event>> = _activeTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                repository.getActiveSegmentsForTrip(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveTrip(trip: Trip) {
        viewModelScope.launch {
            repository.upsertTrip(trip)
        }
    }
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTripById(trip.id)
        }
    }
}

data class DashboardUiState(
    val activeTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<Trip> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

// Simple data class to hold our 3 buckets
data class SegmentGroups(
    val previous: List<EventSummary> = emptyList(),
    val active: List<EventSummary> = emptyList(),
    val upcoming: List<EventSummary> = emptyList()
)

class DashboardViewModelFactory(
    private val repository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
