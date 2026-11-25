package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.dao.BaitDao
import com.funjim.fishstory.database.dao.BodyOfWaterDao
import com.funjim.fishstory.database.dao.EventDao
import com.funjim.fishstory.database.dao.FishDao
import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.dao.LureDao
import com.funjim.fishstory.database.dao.PhotoDao
import com.funjim.fishstory.database.dao.SpeciesDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.dao.TripDao
import com.funjim.fishstory.database.mapper.toSpeciesDomainList
import com.funjim.fishstory.database.mapper.toTripDomainList
import com.funjim.fishstory.model.*
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.List

class MainViewModel(
    private val configRepo: ConfigurationRepository,
    private val locationProvider: LocationProvider,
    private val baitDao: BaitDao,
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val eventDao: EventDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val speciesDao: SpeciesDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {
    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    val appSettings: StateFlow<AppSettings> = configRepo.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    // Call this whenever the app starts up or right after the permission dialog closes
    fun refreshPermissionStatus() {
        _hasLocationPermission.value = locationProvider.hasLocationPermission()
    }

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
        .map { list -> list.toTripDomainList() }

    val species: Flow<List<Species>> = speciesDao.getAllSpecies()
        .map { list -> list.toSpeciesDomainList() }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            configRepo.updateColorTheme(theme)
        }
    }
}

class MainViewModelFactory(
    private val configRepo: ConfigurationRepository,
    private val locationProvider: LocationProvider,
    private val baitDao: BaitDao,
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val eventDao: EventDao,
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val speciesDao: SpeciesDao,
    private val tackleBoxDao: TackleBoxDao,
    private val tripDao: TripDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                configRepo = configRepo,
                locationProvider = locationProvider,
                baitDao = baitDao,
                bodyOfWaterDao = bodyOfWaterDao,
                eventDao = eventDao,
                fishDao = fishDao,
                fishermanDao = fishermanDao,
                lureDao = lureDao,
                photoDao = photoDao,
                speciesDao = speciesDao,
                tackleBoxDao = tackleBoxDao,
                tripDao = tripDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}