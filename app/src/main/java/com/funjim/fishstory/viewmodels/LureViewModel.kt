package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.LureRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map

class LureViewModel(
    private val repository: LureRepository
) : ViewModel() {
    private val _sortOrder = MutableStateFlow(LureSortOrder.NAME)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    private val _lureColors = repository.allLureColors
    val lureColors = _lureColors

    val lurePhotos: StateFlow<Map<String, List<Photo>>> = repository.lurePhotos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap() // <--- This ensures 'by' always has a non-null map to read
        )

    val luresWithName: StateFlow<List<LureWithName>> = repository.getAllLures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine lures, colors, and photos into the UI model
    val luresWithDisplay: StateFlow<List<LureSummaryWithColors>> = combine(
        repository.getAllLureSummaries(),
        repository.allLureColors,
        repository.lurePhotos,
        _sortOrder,
        _isReversed
    ) { lures, colors, photos, sort, reversed ->
        val colorMap = colors.associateBy { it.id }

        val displayList = lures.map { lure ->
            LureSummaryWithColors(
                lureSummary = lure,
                primaryColorName = colorMap[lure.lure.primaryColorId]?.name,
                secondaryColorName = colorMap[lure.lure.secondaryColorId]?.name,
                glowColorName = colorMap[lure.lure.glowColorId]?.name,
                // TODO - enable photos
            )
        }

        val sortedList = applySorting(displayList, sort)
        if (reversed) sortedList.reversed() else sortedList
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList())

    private fun applySorting(list: List<LureSummaryWithColors>, order: LureSortOrder): List<LureSummaryWithColors> {
        return when (order) {
            LureSortOrder.NAME -> list.sortedBy { it.lureSummary.lure.name }
            LureSortOrder.PRIMARY_COLOR -> list.sortedBy { it.primaryColorName }
            LureSortOrder.SECONDARY_COLOR -> list.sortedBy { it.secondaryColorName }
            LureSortOrder.GLOW_COLOR -> list.sortedBy { it.glowColorName }
            LureSortOrder.GLOW -> list.sortedBy { it.lureSummary.lure.glows }
            LureSortOrder.HOOK_TYPE -> list.sortedBy { it.lureSummary.lure.hasSingleHook }
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

    fun addLure(lure: Lure) {
        viewModelScope.launch {
            repository.insertLure(lure)
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

    suspend fun getLureById(id: String): Lure? {
        return repository.getLureById(id)
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
    val tackleBoxWithLures: StateFlow<List<Lure>> = _selectedTackleBoxId
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
    private val repository: LureRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LureViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
