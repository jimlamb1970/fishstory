package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BodyOfWaterViewModel(
    private val configRepo: ConfigurationRepository,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    val appSettings: StateFlow<AppSettings> = configRepo.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bodyOfWaterSummaries: StateFlow<List<BodyOfWaterSummary>> = fishRepo.bodyOfWaterSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBodyOfWater(
        item: BodyOfWater,
        onSuccess: (BodyOfWater) -> Unit
    ) {
        viewModelScope.launch {
            try {
                envRepo.addBodyOfWater(item)
                onSuccess(item)
            } catch (_: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Body of Water '${item.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding body of water.")
            }
        }
    }

    fun updateBodyOfWater(
        item: BodyOfWater,
        onSuccess: (BodyOfWater) -> Unit
    ) {
        viewModelScope.launch {
            try {
            envRepo.updateBodyOfWater(item)
                onSuccess(item)
            } catch (_: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Body of Water '${item.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding body of water.")
            }
        }
    }

    fun deleteBodyOfWater(bodyOfWater: BodyOfWater) {
        viewModelScope.launch {
            envRepo.deleteBodyOfWater(bodyOfWater)
        }
    }

    fun bodyOfWaterThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun deleteBodyOfWaterThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteBodyOfWaterThumbnail(id)
        }
    }

    fun updateBodyOfWaterThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateBodyOfWaterThumbnail(id, uri)
        }
    }
}

class BodyOfWaterViewModelFactory(
    private val configRepo: ConfigurationRepository,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BodyOfWaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BodyOfWaterViewModel(
                configRepo,
                envRepo,
                fishRepo,
                photoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
