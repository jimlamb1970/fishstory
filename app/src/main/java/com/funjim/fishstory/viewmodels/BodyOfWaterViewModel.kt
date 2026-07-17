package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BodyOfWaterViewModel(
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
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

    fun addBodyOfWater(bodyOfWater: BodyOfWater) {
        viewModelScope.launch {
            envRepo.addBodyOfWater(bodyOfWater)
        }
    }

    fun upsertBodyOfWater(bodyOfWater: BodyOfWater) {
        viewModelScope.launch {
            envRepo.upsertBodyOfWater(bodyOfWater)
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
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BodyOfWaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BodyOfWaterViewModel(envRepo, fishRepo, photoRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
