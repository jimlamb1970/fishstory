package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.mapper.toSummary
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TackleBoxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishermanDetailsViewModel(
    private val configRepo: ConfigurationRepository,
    private val fishermanRepo: FishermanRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tackleBoxRepo: TackleBoxRepository
) : ViewModel() {
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    val appSettings: StateFlow<AppSettings> = configRepo.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    fun selectFisherman(id: String) { _selectedFishermanId.value = id }

    private val _showPhotos = MutableStateFlow(false)
    val showPhotos: StateFlow<Boolean> = _showPhotos.asStateFlow()
    fun toggleShowPhotos() {
        _showPhotos.value = !_showPhotos.value
    }

    // Data Flows
    @OptIn(ExperimentalCoroutinesApi::class)
    val statistics: StateFlow<FishermanFullStatistics?> = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { fishermanRepo.getFishermanFullStatistics(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FishermanDetailsUiState> = _selectedFishermanId
        .filter { !it.isNullOrBlank() } // Only proceed when we have a valid ID
        .flatMapLatest { id ->
            combine(
                fishermanRepo.getActiveTripSummariesForFisherman(id!!),
                fishermanRepo.getUpcomingTripSummariesForFisherman(id),
                fishermanRepo.getPastTripSummariesForFisherman(id)
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FishermanDetailsUiState(isLoading = true)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripSummaries: StateFlow<List<TripSummary>> = _selectedFishermanId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else fishermanRepo.getTripSummariesForFisherman(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanPhotos: StateFlow<List<Photo>> = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { photoRepo.getPhotosForFisherman(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun deleteFisherman() {
        val id = _selectedFishermanId.value ?: return
        _selectedFishermanId.value = null
        viewModelScope.launch {
            fishermanRepo.deleteFishermanById(id)
        }
    }
    fun updateFisherman(
        fisherman: Fisherman,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishermanRepo.updateFisherman(fisherman)
                onSuccess()
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Fisherman '${fisherman.fullName}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while updating fisherman.")
            }
        }
    }

    fun addFishermanPhoto(fishermanId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addFishermanPhoto(fishermanId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }

    fun fishermanThumbnail(): Flow<ByteArray?> {
        val id = _selectedFishermanId.value ?: return(flowOf(null))

        return photoRepo.fetchFishermanThumbnail(id)
            .flowOn(Dispatchers.IO)
    }
    fun setFishermanThumbnail(fishermanId: String, photoId: String) {
        viewModelScope.launch { photoRepo.setFishermanThumbnail(fishermanId, photoId) }
    }

    fun deleteFishermanPhoto(fishermanId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteFishermanPhoto(fishermanId, photoId) }
    }

    fun tripThumbnail(tripId: String): Flow<ByteArray?> {
        return photoRepo.fetchTripThumbnail(tripId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun createTackleBox(
        tackleBox: TackleBox,
        onSuccess: (TackleBox) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishermanRepo.insertTackleBox(tackleBox)
                onSuccess(tackleBox)
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Tackle box '${tackleBox.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding tackle box.")
            }
        }
    }

    fun deleteTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch { fishermanRepo.deleteTackleBox(tackleBox) }
    }

    fun getTackleBoxSummary(
        fishermanId: String,
        tripId: String
    ): Flow<TackleBoxSummary> {
        return tackleBoxRepo.getTripTackleBoxSummary(
            fishermanId = fishermanId,
            tripId = tripId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLuresInTackleBox(
        tripId: String,
        fishermanId: String
    ): Flow<List<LureWithColorsSummary>> {
        return fishermanRepo.getTripTackleBoxForFisherman(
            fishermanId = fishermanId,
            tripId = tripId
        ).flatMapLatest { tackleBox ->
            if (tackleBox != null) {
                fishermanRepo.getLuresInTackleBox(tackleBox.id).map { lures ->
                    lures.map { it.toSummary() }
                }
            } else {
                lureRepo.getLureSummariesWithColors(
                    tripId = tripId,
                    fishermanId = fishermanId
                )
            }
        }
    }

    fun getTripTackleBox(tripId: String, fishermanId: String): Flow<TackleBox?> {
        return fishermanRepo.getTripTackleBoxForFisherman(
            fishermanId = fishermanId,
            tripId = tripId
        )
    }
}

data class FishermanDetailsUiState(
    val activeTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<TripSummary> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

class FishermanDetailsViewModelFactory(
    private val configRepo: ConfigurationRepository,
    private val repository: FishermanRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tackleBoxRepo: TackleBoxRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishermanDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishermanDetailsViewModel(
                configRepo,
                repository,
                lureRepo,
                photoRepo,
                tackleBoxRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
