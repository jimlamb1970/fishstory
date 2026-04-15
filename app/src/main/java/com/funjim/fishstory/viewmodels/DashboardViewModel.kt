package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: TripRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getActiveTrip(),
        repository.getUpcomingTrips(),
        repository.getPreviousTrips()
    ) { active, upcoming, previous ->
        DashboardUiState(
            activeTrip = active,
            upcomingTrips = upcoming,
            recentTrips = previous.take(5), // Only show the last 5 on the dashboard
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
}

data class DashboardUiState(
    val activeTrip: Trip? = null,
    val upcomingTrips: List<Trip> = emptyList(),
    val recentTrips: List<Trip> = emptyList(),
    val isLoading: Boolean = false
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
