package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.LocationProvider
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LimitViewModel(
    private val configRepo: ConfigurationRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val appSettings: StateFlow<AppSettings> = configRepo.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLimits: StateFlow<List<Limit>> = fishRepo.getAllLimits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allSpecies: StateFlow<List<Species>> = fishRepo.allSpecies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getSpeciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

    fun addSpecies(
        item: Species,
        onSuccess: (Species) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishRepo.addSpecies(item)
                onSuccess(item)
            } catch (e: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Species '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding species.")
            }
        }
    }

    // Limit functions
    fun addLimit(limit: Limit) {
        viewModelScope.launch {
            fishRepo.addLimit(limit)
        }
    }

    fun deleteLimit(limit: Limit) {
        viewModelScope.launch {
            fishRepo.deleteLimit(limit)
        }
    }
}

class LimitViewModelFactory(
    private val configurationRepository: ConfigurationRepository,
    private val fishRepository: FishRepository,
    private val photoRepository: PhotoRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(LimitViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return LimitViewModel(
                configurationRepository,
                fishRepository,
                photoRepository,
                savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
