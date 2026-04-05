package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishermanListViewModel(
    private val fishermanDao: FishermanDao
) : ViewModel() {

    private val _rawSummaries = fishermanDao.getFishermanSummaries()

    private val _sortOrder = MutableStateFlow(FishermanSortOrder.NAME_AZ)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    val fishermanSummaries: StateFlow<List<FishermanSummary>> = combine(
        _rawSummaries,
        _sortOrder,
        _isReversed
    ) { summaries, order, reversed ->
        val sorted = when (order) {
            FishermanSortOrder.NAME_AZ -> summaries.sortedBy { it.fisherman.fullName.lowercase() }
            FishermanSortOrder.MOST_CATCHES -> summaries.sortedByDescending { it.totalCatches }
/* -- save these for later
            FishermanSortOrder.MOST_RELEASED -> summaries.sortedByDescending { it.totalReleased }
            FishermanSortOrder.MOST_TRIPS -> summaries.sortedByDescending { it.totalTrips }
 */
        }

        if (reversed) sorted.reversed() else sorted
    }
        .stateIn(
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

    suspend fun addFisherman(fisherman: Fisherman) {
        fishermanDao.insert(fisherman)
    }

    fun deleteFisherman(fisherman: Fisherman) {
        viewModelScope.launch {
            fishermanDao.deleteFisherman(fisherman)
        }
    }
}

class FishermanListViewModelFactory(
    private val fishermanDao: FishermanDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishermanListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishermanListViewModel(fishermanDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
