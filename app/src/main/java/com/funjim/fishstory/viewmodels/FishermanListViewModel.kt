package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FishermanListViewModel(
    private val fishermanRepo: FishermanRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(FishermanSortOrder.NAME_AZ)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    // We use flatMapLatest so that whenever sortOrder or isReversed changes,
    // the repository fetches a new sorted flow.
    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanSummaries: StateFlow<List<FishermanSummary>> =
        combine(_sortOrder, _isReversed) { order, reversed ->
            order to reversed
        }.flatMapLatest { (order, reversed) ->
            fishermanRepo.getSortedFishermanSummaries(order, reversed)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleReverse() {
        _isReversed.value = !_isReversed.value
    }

    fun updateSortOrder(newOrder: FishermanSortOrder) {
        _sortOrder.value = newOrder
    }

    fun addFisherman(fisherman: Fisherman) {
        viewModelScope.launch {
            fishermanRepo.addFisherman(fisherman)
        }
    }

    fun deleteFisherman(fisherman: Fisherman) {
        viewModelScope.launch {
            fishermanRepo.deleteFisherman(fisherman)
        }
    }

    fun fishermanThumbnail(fishermanId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(fishermanId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }
}

class FishermanListViewModelFactory(
    private val fishermanRepo: FishermanRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishermanListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishermanListViewModel(
                fishermanRepo = fishermanRepo,
                photoRepo = photoRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}