package com.funjim.fishstory.viewmodels

import android.content.Context
import android.net.Uri
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
import com.funjim.fishstory.database.entity.BaitEntity
import com.funjim.fishstory.database.entity.BodyOfWaterEntity
import com.funjim.fishstory.database.entity.EventBaitEntity
import com.funjim.fishstory.database.entity.EventBodyOfWaterEntity
import com.funjim.fishstory.database.entity.EventEntity
import com.funjim.fishstory.database.entity.EventFishermanEntity
import com.funjim.fishstory.database.entity.EventTargetSpeciesEntity
import com.funjim.fishstory.database.entity.FishEntity
import com.funjim.fishstory.database.entity.FishermanEntity
import com.funjim.fishstory.database.entity.LureColorEntity
import com.funjim.fishstory.database.entity.LureEntity
import com.funjim.fishstory.database.entity.LureGlowColorEntity
import com.funjim.fishstory.database.entity.LurePrimaryColorEntity
import com.funjim.fishstory.database.entity.LureSecondaryColorEntity
import com.funjim.fishstory.database.entity.PhotoBaitEntity
import com.funjim.fishstory.database.entity.PhotoBodyOfWaterEntity
import com.funjim.fishstory.database.entity.PhotoEntity
import com.funjim.fishstory.database.entity.PhotoEventEntity
import com.funjim.fishstory.database.entity.PhotoFishEntity
import com.funjim.fishstory.database.entity.PhotoFishermanEntity
import com.funjim.fishstory.database.entity.PhotoLureEntity
import com.funjim.fishstory.database.entity.PhotoSpeciesEntity
import com.funjim.fishstory.database.entity.PhotoTripEntity
import com.funjim.fishstory.database.entity.SpeciesEntity
import com.funjim.fishstory.database.entity.TackleBoxEntity
import com.funjim.fishstory.database.entity.TackleBoxLureEntity
import com.funjim.fishstory.database.entity.TripBaitEntity
import com.funjim.fishstory.database.entity.TripBodyOfWaterEntity
import com.funjim.fishstory.database.entity.TripEntity
import com.funjim.fishstory.database.entity.TripFishermanEntity
import com.funjim.fishstory.database.entity.TripTargetSpeciesEntity
import com.funjim.fishstory.database.mapper.toEventDomainList
import com.funjim.fishstory.database.mapper.toFishWithDetailsDomainList
import com.funjim.fishstory.database.mapper.toSpeciesDomainList
import com.funjim.fishstory.database.mapper.toTripDomainList
import com.funjim.fishstory.model.*
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.collections.List

@Serializable
data class DatabaseExportData(
    val trips: List<TripEntity> = emptyList(),
    val events: List<EventEntity> = emptyList(),
    val baits: List<BaitEntity> = emptyList(),
    val eventBait: List<EventBaitEntity> = emptyList(),
    val tripBait: List<TripBaitEntity> = emptyList(),
    val bodiesOfWater: List<BodyOfWaterEntity> = emptyList(),
    val eventBodyOfWater: List<EventBodyOfWaterEntity> = emptyList(),
    val tripBodyOfWater: List<TripBodyOfWaterEntity> = emptyList(),
    val fishermen: List<FishermanEntity> = emptyList(),
    val tripFishermen: List<TripFishermanEntity> = emptyList(),
    val eventFishermen: List<EventFishermanEntity> = emptyList(),
    val colors: List<LureColorEntity> = emptyList(),
    val lures: List<LureEntity> = emptyList(),
    val lurePrimaryColors: List<LurePrimaryColorEntity> = emptyList(),
    val lureSecondaryColors: List<LureSecondaryColorEntity> = emptyList(),
    val lureGlowColors: List<LureGlowColorEntity> = emptyList(),
    val tackleboxes: List<TackleBoxEntity> = emptyList(),
    val tackleBoxLures: List<TackleBoxLureEntity> = emptyList(),
    val species: List<SpeciesEntity> = emptyList(),
    val eventTargetSpecies: List<EventTargetSpeciesEntity> = emptyList(),
    val tripTargetSpecies: List<TripTargetSpeciesEntity> = emptyList(),
    val fish: List<FishEntity> = emptyList(),
    val photos: List<PhotoEntity> = emptyList(),
    val photosBait: List<PhotoBaitEntity> = emptyList(),
    val photosBodyOfWater: List<PhotoBodyOfWaterEntity> = emptyList(),
    val photosEvent: List<PhotoEventEntity> = emptyList(),
    val photosFish: List<PhotoFishEntity> = emptyList(),
    val photosFisherman: List<PhotoFishermanEntity> = emptyList(),
    val photosLure: List<PhotoLureEntity> = emptyList(),
    val photosSpecies: List<PhotoSpeciesEntity> = emptyList(),
    val photosTrip: List<PhotoTripEntity> = emptyList()
)

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

    private val json = Json { prettyPrint = true }
    private val jsonImport = Json { ignoreUnknownKeys = true }

    private val _selectEvent = MutableStateFlow(0)
    val selectEvent = _selectEvent.asStateFlow()

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
        .map { list -> list.toTripDomainList() }

    val species: Flow<List<Species>> = speciesDao.getAllSpecies()
        .map { list -> list.toSpeciesDomainList() }
    val allFish: Flow<List<FishWithDetails>> =
        fishDao.getFishWithDetails(null, null, null, null, null)
            .map { list -> list.toFishWithDetailsDomainList() }

    fun getSegmentsForTrip(tripId: String): Flow<List<Event>> {
        return eventDao.getEventsForTrip(tripId)
            .map { list -> list.toEventDomainList() }
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
            .map { list -> list.toFishWithDetailsDomainList() }
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForEvent(segmentId)
            .map { list -> list.toFishWithDetailsDomainList() }
    }


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