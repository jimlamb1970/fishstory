package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.SegmentDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishViewModel(
    private val fishDao: FishDao,
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
    private val photoDao: PhotoDao
) : ViewModel() {

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
    val species: Flow<List<Species>> = fishDao.getAllSpecies()

    private val _selectedTripIdForFilter = MutableStateFlow<String?>(null)
    val selectedTripIdForFilter = _selectedTripIdForFilter.asStateFlow()

    private val _selectedSegmentIdForFilter = MutableStateFlow<String?>(null)
    val selectedSegmentIdForFilter = _selectedSegmentIdForFilter.asStateFlow()

    fun updateSelectedTripIdForFilter(id: String?) {
        _selectedTripIdForFilter.value = id
        _selectedSegmentIdForFilter.value = null
    }

    fun updateSelectedSegmentIdForFilter(id: String?) {
        _selectedSegmentIdForFilter.value = id
    }

    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>> {
        return segmentDao.getSegmentsForTrip(tripId)
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForSegment(segmentId)
    }

    suspend fun getFishById(id: String): Fish? {
        return fishDao.getFishById(id)
    }

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    private data class FishFilterParams(
        val tripId: String?,
        val segmentId: String?,
        val sortOrder: FishSortOrder,
        val isReversed: Boolean
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        selectedTripIdForFilter,
        selectedSegmentIdForFilter,
        _sortOrder,
        _isReversed
    ) { tripId, segId, sortOrder, isReversed ->
        FishFilterParams(tripId, segId, sortOrder, isReversed)
    }.flatMapLatest { params ->
        val baseFlow = when {
            !params.segmentId.isNullOrBlank() -> getFishForSegment(params.segmentId)
            !params.tripId.isNullOrBlank() -> getFishForTrip(params.tripId)
            else -> flowOf(emptyList())
        }
        baseFlow.map { list ->
            val sortedList = when (params.sortOrder) {
                FishSortOrder.TIMESTAMP_NEWEST_FIRST -> list.sortedByDescending { it.timestamp }
                FishSortOrder.FISHERMAN_AZ -> list.sortedBy { it.fishermanName }
                FishSortOrder.SPECIES_AZ -> list.sortedBy { it.speciesName }
                FishSortOrder.LENGTH_LONGEST_FIRST -> list.sortedByDescending { it.length }
                FishSortOrder.HOLE_NUMBER_ASC -> list.sortedBy { it.holeNumber }
                FishSortOrder.RELEASED -> list.sortedBy { it.isReleased }
                FishSortOrder.LURE -> list.sortedBy { it.getFullLureName() }
            }

            if (params.isReversed) sortedList.reversed() else sortedList
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleReverse() {
        _isReversed.value = !_isReversed.value
    }

    fun updateSortOrder(newOrder: FishSortOrder) {
        _sortOrder.value = newOrder
    }

    fun deleteFishObject(fish: Fish) {
        viewModelScope.launch {
            fishDao.deleteFish(fish)
        }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishDao.insertSpecies(species)
        }
    }

    fun deleteSpecies(species: Species) {
        viewModelScope.launch {
            fishDao.deleteSpecies(species)
        }
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
    val fishPhotos: StateFlow<Map<String, List<Photo>>> = photoDao.getAllFishPhotos()
        .map { photos ->
            photos.filter { it.fishId != null }
                .groupBy { it.fishId!! } // The !! is safe here because of the filter
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )
}

class FishViewModelFactory(
    private val fishDao: FishDao,
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
    private val photoDao: PhotoDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishViewModel(fishDao, tripDao, segmentDao, photoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
