package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
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

class SkyConditionViewModel(
    private val envRepo: EnvironmentRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    val allSkyConditions: StateFlow<List<SkyCondition>> = envRepo.allSkyConditions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSkyCondition(
        item: SkyCondition,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                envRepo.addSkyCondition(item)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _toastMessage.emit("Sky Condition '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding sky condition.")
            }
        }
    }

    fun updateSkyCondition(
        item: SkyCondition,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                envRepo.updateSkyCondition(item)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _toastMessage.emit("Sky Condition '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while updating sky condition.")
            }
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
