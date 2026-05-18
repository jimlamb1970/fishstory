package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoMetadata
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.ui.utils.sortLures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LureViewModel(
    private val repository: LureRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
    private val _sortOrder = MutableStateFlow(LureSortOrder.NAME)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    private val _lureColors = repository.allLureColors
    val lureColors = _lureColors

    // Combine lures, colors, and photos into the UI model
    val luresWithDisplay: StateFlow<List<LureSummaryWithColors>> = combine(
        repository.getLureSummariesWithColors(),
        _sortOrder,
        _isReversed
    ) { lures, sort, reversed ->
        val sortedList = applySorting(lures, sort)
        if (reversed) sortedList.reversed() else sortedList
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList())

    private fun applySorting(list: List<LureSummaryWithColors>, order: LureSortOrder): List<LureSummaryWithColors> {
        return when (order) {
            LureSortOrder.NAME -> sortLures(list, order)
            LureSortOrder.PRIMARY_COLOR -> sortLures(list, order)
            LureSortOrder.SECONDARY_COLOR -> sortLures(list, order)
            LureSortOrder.GLOW_COLOR -> sortLures(list, order)
            LureSortOrder.GLOW -> list.sortedBy { it.lure.glows }
            LureSortOrder.HOOK_TYPE -> list.sortedBy { it.lure.hasSingleHook }
        }
    }

    fun toggleReverse() { _isReversed.value = !_isReversed.value }
    fun setSortOrder(order: LureSortOrder) { _sortOrder.value = order }

    fun addLureToTackleBox(tackleBoxId: String, lureId: String) {
        viewModelScope.launch {
            repository.addLureToTackleBox(tackleBoxId, lureId)
        }
    }

    fun removeLureFromTackleBox(tackleBoxId: String, lureId: String) {
        viewModelScope.launch {
            repository.removeLureFromTackleBox(tackleBoxId, lureId)
        }
    }

    fun upsertLure(lure: Lure) {
        viewModelScope.launch {
            repository.upsertLure(lure)
        }
    }

    fun deleteLure(lure: Lure) {
        viewModelScope.launch {
            repository.deleteLure(lure)
        }
    }

    fun addLureColor(color: LureColor) {
        viewModelScope.launch {
            repository.insertLureColor(color)
        }
    }

    fun upsertLureColor(color: LureColor) {
        viewModelScope.launch {
            repository.upsertLureColor(color)
        }
    }

    fun deleteLureColor(color: LureColor) {
        viewModelScope.launch {
            repository.deleteLureColor(color)
        }
    }

    suspend fun getLureWithPhotos(id: String): LureWithPhotos? {
        return repository.getLureWithPhotos(id)
    }

    fun lureThumbnail(lureId: String): Flow<ByteArray?> {
        return photoRepo.fetchLureThumbnail(lureId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    suspend fun doesPhotoExist(uri: Uri): Boolean {
        return photoRepo.doesPhotoExist(uri)
    }
    suspend fun getPhotoMetadata(uri: Uri): PhotoMetadata {
        return photoRepo.getPhotoMetadata(uri)
    }

    fun addLurePhotos(lureId: String, photos: List<Photo>) {
        viewModelScope.launch {
            photoRepo.addLurePhotos(lureId, photos)
        }
    }
    fun deleteLurePhotos(lureId: String, photos: List<Photo>) {
        viewModelScope.launch {
            photoRepo.deleteLurePhotos(lureId, photos)
        }
    }

    private val _selectedFisherman = MutableStateFlow<Fisherman?>(null)
    val selectedFisherman = _selectedFisherman.asStateFlow()

    fun selectFisherman(id: String) {
        viewModelScope.launch {
            // This is the 'suspend' call to the database
            val fisherman = repository.getFishermanById(id)
            _selectedFisherman.value = fisherman
        }
    }

    private val _selectedTackleBoxId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTackleBox: StateFlow<TackleBox?> = _selectedTackleBoxId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getTackleBoxById(id) // Room DAO should return Flow<TackleBox>
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun selectTackleBox(id: String) {
        _selectedTackleBoxId.value = id
    }

    fun updateTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            repository.updateTackleBox(tackleBox)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tackleBoxWithLures: StateFlow<List<LureWithColors>> = _selectedTackleBoxId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                repository.getLuresInTackleBox(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class LureViewModelFactory(
    private val repository: LureRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LureViewModel(repository, photoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
