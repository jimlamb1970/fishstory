package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LureViewModel(
    private val lureDao: LureDao,
    private val fishermanDao: FishermanDao,
    private val tackleBoxDao: TackleBoxDao,
    private val photoDao: PhotoDao
) : ViewModel() {

    val lures: Flow<List<Lure>> = lureDao.getAllLures()
    val lureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()
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
