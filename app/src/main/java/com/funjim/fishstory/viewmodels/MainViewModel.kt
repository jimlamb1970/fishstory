package com.funjim.fishstory.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import com.funjim.fishstory.model.Trip
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.collections.List

// Placeholder data structure for serialization example. Replace with actual DB entities.
@Serializable
data class DatabaseExportData(
    val trips: List<Trip>,
    val events: List<Event>,
    val fishermen: List<Fisherman>,
    val tripFishermanCrossRef: List<TripFishermanCrossRef>,
    val eventFishermanCrossRef: List<EventFishermanCrossRef>,
    val lures: List<Lure>,
    val tackleboxes: List<TackleBox>,
    val tackleBoxLureCrossRef: List<TackleBoxLureCrossRef>,
    val colors: List<LureColor>,
    val fish: List<Fish>,
    val species: List<Species>,
    val photos: List<Photo>
)

class MainViewModel(
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val eventDao: EventDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {
    // A simple flow to broadcast volume key directions: 1 for Down, Select for Up
    private val _volumeKeyEvent = MutableStateFlow(0)
    val volumeKeyEvent = _volumeKeyEvent.asStateFlow()

    private val json = Json { prettyPrint = true }
    private val json_import = Json { ignoreUnknownKeys = true }

    fun onVolumeKeyPressed(direction: Int) {
        viewModelScope.launch {
            // We update the value to trigger the Collector in the UI.
            // Even if the direction is the same, we want a "new" event.
            _volumeKeyEvent.value = direction
            kotlinx.coroutines.delay(50)
            // Reset it immediately so the next press (even if same direction) is caught
            _volumeKeyEvent.value = 0
        }
    }

    private val _selectEvent = MutableStateFlow(0)
    val selectEvent = _selectEvent.asStateFlow()

    fun triggerSelect() {
        viewModelScope.launch {
            _selectEvent.value += 1
        }
    }

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()

    val lureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()
    val species: Flow<List<Species>> = fishDao.getAllSpecies()
    val activeSegments: Flow<List<Event>> = eventDao.getActiveEvents()
    val allFish: Flow<List<FishWithDetails>> =
        fishDao.getFishWithDetails(null, null, null, null)

    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
    }

    fun getSegmentsForTrip(tripId: String): Flow<List<Event>> {
        return eventDao.getEventsForTrip(tripId)
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForEvent(segmentId)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

    // --- NEW METHODS FOR SETTINGS SCREEN ---

    suspend fun exportDatabaseAsJson(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val allData = DatabaseExportData(
                    trips = tripDao.getAllTrips().firstOrNull() ?: emptyList(),
                    events = eventDao.getAllEvents().firstOrNull() ?: emptyList(),
                    fishermen = fishermanDao.getAllFishermen().firstOrNull() ?: emptyList(),
                    tripFishermanCrossRef = tripDao.getAllTripFishermanCrossRefs().firstOrNull() ?: emptyList(),
                    eventFishermanCrossRef = eventDao.getAllEventFishermanCrossRefs().firstOrNull() ?: emptyList(),
                    colors = lureDao.getAllLureColors().firstOrNull() ?: emptyList(),
                    lures = lureDao.getAllLures().firstOrNull() ?: emptyList(),
                    tackleboxes = tackleBoxDao.getAllTackleBoxes().firstOrNull() ?: emptyList(),
                    tackleBoxLureCrossRef = tackleBoxDao.getAllTackleBoxLureCrossRefs().firstOrNull() ?: emptyList(),
                    species = fishDao.getAllSpecies().firstOrNull() ?: emptyList(),
                    fish = fishDao.getAllFish().firstOrNull() ?: emptyList(),
                    photos = photoDao.getAllPhotos().firstOrNull() ?: emptyList()
                )

                val jsonString = json.encodeToString(allData)
                context.contentResolver.openOutputStream(uri)?.use { it.write(jsonString.toByteArray()) }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    suspend fun importDatabaseFromJson(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val data = json_import.decodeFromString<DatabaseExportData>(jsonString)

                    tripDao.deleteAllTrips()
                    eventDao.deleteAllEvents()
                    fishermanDao.deleteAllFishermen()
                    tripDao.deleteAllTripFishermanCrossRefs()
                    eventDao.deleteAllEventFishermanCrossRefs()
                    lureDao.deleteAllLures()
                    tackleBoxDao.deleteAllTackleBoxes()
                    tackleBoxDao.deleteAllTackleBoxLureCrossRefs()
                    lureDao.deleteAllLureColors()
                    fishDao.deleteAllFish()
                    fishDao.deleteAllSpecies()
                    photoDao.deleteAllPhotos()

                    data.trips.forEach { tripDao.insertTrip(it) }
                    data.events.forEach { eventDao.insertEvent(it) }
                    data.fishermen.forEach { fishermanDao.insertFisherman(it) }
                    data.tackleboxes.forEach { tackleBoxDao.insertTackleBox(it) }
                    data.tripFishermanCrossRef.forEach { tripDao.insertCrossRef(it) }
                    data.eventFishermanCrossRef.forEach { eventDao.insertEventFishermanCrossRef(it) }
                    data.colors.forEach { lureDao.insertLureColor(it) }
                    data.lures.forEach {
                        val primaryColorId = it.primaryColorId
                        val secondaryColorId = it.secondaryColorId
                        val glowColorId = it.glowColorId

                        val newLure = it.copy(primaryColorId = null, secondaryColorId = null, glowColorId = null)
                        lureDao.upsertLure(newLure)
                        if (primaryColorId != null) {
                            lureDao.upsertLurePrimaryColorCrossRef(LurePrimaryColorCrossRef(it.id, primaryColorId))
                        }
                        if (secondaryColorId != null) {
                            lureDao.upsertLureSecondaryColorCrossRef(LureSecondaryColorCrossRef(it.id, secondaryColorId))
                        }
                        if (glowColorId != null) {
                            lureDao.upsertLureGlowColorCrossRef(LureGlowColorCrossRef(it.id, glowColorId))
                        }
                    }

                    data.tackleBoxLureCrossRef.forEach { tackleBoxDao.insertLureToTackleBox(it) }
                    data.species.forEach { fishDao.insertSpecies(it) }
                    data.fish.forEach { fishDao.insertFish(it) }
                    data.photos.forEach { photoDao.insertPhoto(it) }
                }
                // For simplicity, assume success, but you should check deserialization status
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}

class MainViewModelFactory(
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val eventDao: EventDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tripDao, fishermanDao, eventDao, lureDao, fishDao, photoDao, tackleBoxDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}