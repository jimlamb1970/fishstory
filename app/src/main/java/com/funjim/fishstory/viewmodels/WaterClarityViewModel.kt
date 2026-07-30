package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WaterClarityViewModel(
    private val envRepo: EnvironmentRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
    val allWaterClarity: StateFlow<List<WaterClarity>> = envRepo.allWaterClarity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWaterClarity(waterClarity: WaterClarity) {
        viewModelScope.launch {
            envRepo.addWaterClarity(waterClarity)
        }
    }

    fun upsertWaterClarity(bait: WaterClarity) {
        viewModelScope.launch {
            envRepo.upsertWaterClarity(bait)
        }
    }

    fun deleteWaterClarity(bait: WaterClarity) {
        viewModelScope.launch {
            envRepo.deleteWaterClarity(bait)
        }
    }

    fun waterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchWaterClarityThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun deleteWaterClarityThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteWaterClarityThumbnail(id)
        }
    }

    fun updateWaterClarityThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateWaterClarityThumbnail(id, uri)
        }
    }
}

class WaterClarityViewModelFactory(
    private val envRepo: EnvironmentRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterClarityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterClarityViewModel(
                envRepo = envRepo,
                photoRepo = photoRepo
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
