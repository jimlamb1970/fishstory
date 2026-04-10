package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishermanDetailsViewModel(
    private val fishermanDao: FishermanDao,
    private val tripDao: TripDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
    var lureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()

    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    fun selectFisherman(id: String) {
        _selectedFishermanId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val statistics: StateFlow<FishermanFullStatistics?> = _selectedFishermanId
        .filterNotNull() // Only query if we have an ID
        .flatMapLatest { id ->
            fishermanDao.getFishermanFullStatistics(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripSummaries: StateFlow<List<FishermanTripSummary>> = _selectedFishermanId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                fishermanDao.getTripSummariesForFisherman(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getPhotosForFisherman(fishermanId: String): Flow<List<Photo>> {
        return photoDao.getPhotosForFisherman(fishermanId)
    }

    fun getPhotosForLure(lureId: String): Flow<List<Photo>> {
        return photoDao.getPhotosForLure(lureId)
    }

    fun addPhoto(photo: Photo) {
        viewModelScope.launch {
            photoDao.insertPhoto(photo)
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            photoDao.deletePhoto(photo)
        }
    }

    fun updateFisherman(fisherman: Fisherman) {
        viewModelScope.launch {
            fishermanDao.update(fisherman)
        }
    }

    fun createTackleBox(fishermanId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            tackleBoxDao.insertTackleBox(tackleBox)
        }
    }

    fun deleteTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            tackleBoxDao.deleteTackleBox(tackleBox)
        }
    }

    val lurePhotos: StateFlow<Map<String, List<Photo>>> = photoDao.getAllLurePhotos()
        .map { photos ->
            photos.filter { it.lureId != null }
                .groupBy { it.lureId!! } // The !! is safe here because of the filter
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}

class FishermanDetailsViewModelFactory(
    private val fishermanDao: FishermanDao,
    private val tripDao: TripDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishermanDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishermanDetailsViewModel(fishermanDao, tripDao, lureDao, photoDao, tackleBoxDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
