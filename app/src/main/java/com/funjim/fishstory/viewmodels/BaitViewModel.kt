package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BaitViewModel(
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository
) : ViewModel() {
    val allBaits: StateFlow<List<Bait>> = lureRepo.allBaits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val baitSummaries: StateFlow<List<BaitSummary>> = fishRepo.baitSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBait(bait: Bait) {
        viewModelScope.launch {
            lureRepo.addBait(bait)
        }
    }

    fun upsertBait(bait: Bait) {
        viewModelScope.launch {
            lureRepo.upsertBait(bait)
        }
    }

    fun deleteBait(bait: Bait) {
        viewModelScope.launch {
            lureRepo.deleteBait(bait)
        }
    }

    fun baitThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchBaitThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun deleteBaitThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteBaitThumbnail(id)
        }
    }

    fun updateBaitThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateBaitThumbnail(id, uri)
        }
    }
}

class BaitViewModelFactory(
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BaitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BaitViewModel(
                fishRepo = fishRepo,
                lureRepo = lureRepo,
                photoRepo = photoRepo
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
