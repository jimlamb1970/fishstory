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

class SkyConditionViewModel(
    private val envRepo: EnvironmentRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
    val allSkyConditions: StateFlow<List<SkyCondition>> = envRepo.allSkyConditions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSkyCondition(skyCondition: SkyCondition) {
        viewModelScope.launch {
            envRepo.addSkyCondition(skyCondition)
        }
    }

    fun upsertSkyCondition(skyCondition: SkyCondition) {
        viewModelScope.launch {
            envRepo.upsertSkyCondition(skyCondition)
        }
    }

    fun deleteSkyCondition(skyCondition: SkyCondition) {
        viewModelScope.launch {
            envRepo.deleteSkyCondition(skyCondition)
        }
    }

    fun skyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSkyConditionThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun deleteSkyConditionThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteSkyConditionThumbnail(id)
        }
    }

    fun updateSkyConditionThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateSkyConditionThumbnail(id, uri)
        }
    }
}

class SkyConditionViewModelFactory(
    private val envRepo: EnvironmentRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SkyConditionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SkyConditionViewModel(
                envRepo = envRepo,
                photoRepo = photoRepo
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
