package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripListViewModel(
    private val locationProvider: LocationProvider,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val _tripFilter = MutableStateFlow(TripListFilter.COMPLETED)
    val tripFilter = _tripFilter.asStateFlow()
    fun updateTripFilter(filter: TripListFilter) { _tripFilter.value = filter }

    val uiState: StateFlow<TripListUiState> = combine(
        tripRepo.getUpcomingTripSummaries(),
        tripRepo.getActiveTripSummaries(),
        tripRepo.getPreviousTripSummaries()
    ) { upcoming, active, previous ->
        TripListUiState(
            upcomingTrips = upcoming,
            liveTrips = active,
            completedTrips = previous,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripListUiState(isLoading = true)
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
    val upcomingTrips: List<TripSummary> = emptyList(),
    val liveTrips: List<TripSummary> = emptyList(),
    val completedTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

class TripListViewModelFactory(
    private val locationProvider: LocationProvider,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripListViewModel(locationProvider, tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
