package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: TripRepository
) : ViewModel() {
    private val currentTime = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
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
    val activeTripSegments: StateFlow<SegmentGroups> = currentTime
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
