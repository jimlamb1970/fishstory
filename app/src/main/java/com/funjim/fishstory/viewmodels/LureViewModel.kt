package com.funjim.fishstory.viewmodels

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import com.funjim.fishstory.ui.screens.LureWithDisplay
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
import kotlin.collections.map

class LureViewModel(
    private val lureDao: LureDao,
    private val fishermanDao: FishermanDao,
    private val tackleBoxDao: TackleBoxDao,
    private val photoDao: PhotoDao
) : ViewModel() {
    private val _rawLures = lureDao.getAllLures()

    private val _sortOrder = MutableStateFlow(LureSortOrder.NAME)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    private val _lureColors = lureDao.getAllLureColors()
    val lureColors = _lureColors

    val lures: StateFlow<List<LureWithDisplay>> = combine(
        _rawLures,
        _sortOrder,
        _isReversed,
        _lureColors
    ) { lures, order, reversed, colors ->
        val mapped = lures.map { lure ->
            val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
            val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
            val glowColorName = colors.find { it.id == lure.glowColorId }?.name
            LureWithDisplay(
                lure = lure,
                primaryColorName = primaryColorName,
                secondaryColorName = secondaryColorName,
                glowColorName = glowColorName,
                displayName = lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)
            )
        }

        val sorted = when (order) {
            LureSortOrder.NAME         -> mapped.sortedBy { it.displayName }
            LureSortOrder.PRIMARY_COLOR -> mapped.sortedBy { it.primaryColorName ?: "" }
            LureSortOrder.SECONDARY_COLOR -> mapped.sortedBy { it.secondaryColorName ?: "" }
            LureSortOrder.GLOW_COLOR    -> mapped.sortedBy { it.glowColorName ?: "" }
            LureSortOrder.GLOW         -> mapped.sortedBy { it.lure.glows }
            LureSortOrder.HOOK_TYPE     -> mapped.sortedBy { it.lure.hasSingleHook }
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

    fun updateSortOrder(newOrder: LureSortOrder) {
        _sortOrder.value = newOrder
    }

    val fishermen: Flow<List<Fisherman>> = fishermanDao.getAllFishermen()

    val lurePhotos: StateFlow<Map<String, List<Photo>>> = photoDao.getAllPhotos()
        .map { photos -> photos.filter { it.lureId != null }.groupBy { it.lureId!! } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getLuresForFisherman(fishermanId: String): Flow<List<Lure>> {
        return tackleBoxDao.getLuresForFisherman(fishermanId)
    }

    suspend fun getLureById(id: String): Lure? {
        return lureDao.getLureById(id)
    }

    fun addLure(lure: Lure) {
        viewModelScope.launch {
            lureDao.insertLure(lure)
        }
    }

    fun deleteLure(lure: Lure) {
        viewModelScope.launch {
            lureDao.deleteLure(lure)
        }
    }

    fun addLureColor(color: LureColor) {
        viewModelScope.launch {
            lureDao.insertLureColor(color)
        }
    }

    fun deleteLureColor(color: LureColor) {
        viewModelScope.launch {
            lureDao.deleteLureColor(color)
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

    private val _selectedFisherman = MutableStateFlow<Fisherman?>(null)
    val selectedFisherman = _selectedFisherman.asStateFlow()

    fun selectFisherman(id: String) {
        viewModelScope.launch {
            // This is the 'suspend' call to the database
            val fisherman = fishermanDao.getFishermanById(id)
            _selectedFisherman.value = fisherman
        }
    }

    private val _selectedTackleBoxId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tackleBoxesWithLures: StateFlow<List<Lure>> = _selectedTackleBoxId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                tackleBoxDao.getLuresInTackleBox(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTackleBox(id: String) {
        _selectedTackleBoxId.value = id
    }

    fun addLureToFishermanTackleBox(fishermanId: String, lureId: String) {
        viewModelScope.launch {
            // Need to get or create tacklebox
            var tackleBox = tackleBoxDao.getExistingTackleBoxForFisherman(fishermanId)
            if (tackleBox == null) {
                tackleBox = TackleBox(fishermanId = fishermanId)
                tackleBoxDao.insertTackleBox(tackleBox)
            }
            tackleBoxDao.insertLureToTackleBox(TackleBoxLureCrossRef(tackleBox.id, lureId))
        }
    }

    fun removeLureFromFishermanTackleBox(fishermanId: String, lureId: String) {
        viewModelScope.launch {
            val tackleBox = tackleBoxDao.getExistingTackleBoxForFisherman(fishermanId)
            if (tackleBox != null) {
                tackleBoxDao.removeLureFromTackleBox(TackleBoxLureCrossRef(tackleBox.id, lureId))
            }
        }
    }
}

class LureViewModelFactory(
    private val lureDao: LureDao,
    private val fishermanDao: FishermanDao,
    private val tackleBoxDao: TackleBoxDao,
    private val photoDao: PhotoDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LureViewModel(lureDao, fishermanDao, tackleBoxDao, photoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
