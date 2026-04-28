package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishermanRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishermanDetailsViewModel(
    private val repository: FishermanRepository
) : ViewModel() {

    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    fun selectFisherman(id: String) { _selectedFishermanId.value = id }

    // Data Flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val statistics: StateFlow<FishermanFullStatistics?> = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { repository.getFishermanFullStatistics(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FishermanDetailsUiState> = _selectedFishermanId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) {
                flowOf(FishermanDetailsUiState(isLoading = true))
            } else {
                combine(
                    repository.getActiveTripSummariesForFisherman(id),
                    repository.getUpcomingTripSummariesForFisherman(id),
                    repository.getPastTripSummariesForFisherman(id)
                ) { active, upcoming, previous ->
                    FishermanDetailsUiState(
                        activeTrips = active,
                        upcomingTrips = upcoming,
                        recentTrips = previous,
                        // TODO -- add limit of 5 and give the ability to see the full list
//                        recentTrips = previous.take(5), // Slice here to keep the dashboard lean
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FishermanDetailsUiState(isLoading = true)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripSummaries: StateFlow<List<TripSummary>> = _selectedFishermanId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getTripSummariesForFisherman(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanPhotos: StateFlow<List<Photo>> = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { repository.getPhotosForFisherman(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lurePhotos: StateFlow<Map<String, List<Photo>>> = repository.lurePhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getLureNames(tackleBoxId: String?): Flow<List<String>> {
        return repository.getLureNamesInTackleBox(tackleBoxId)
    }
    // In your ViewModel
    fun getFormattedLureList(tackleBoxId: String): StateFlow<String> {
        return getLureNames(tackleBoxId)
            .map { names ->
                if (names.isEmpty()) "No lures in this box"
                else names.joinToString(separator = "\n", limit = 8, truncated = "...and more") { lureName ->
                    "• $lureName"
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "Loading lures..."
            )
    }

    // Actions
    fun updateFisherman(fisherman: Fisherman) {
        viewModelScope.launch { repository.updateFisherman(fisherman) }
    }

    fun addPhoto(photo: Photo) {
        viewModelScope.launch { repository.addPhoto(photo) }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch { repository.deletePhoto(photo) }
    }

    fun createTackleBox(fishermanId: String, name: String) {
        viewModelScope.launch { repository.createTackleBox(fishermanId, name) }
    }

    fun deleteTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch { repository.deleteTackleBox(tackleBox) }
    }
}
data class FishermanDetailsUiState(
    val activeTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<TripSummary> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

class FishermanDetailsViewModelFactory(
    private val repository: FishermanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishermanDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishermanDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
