package com.funjim.fishstory.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
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
    val colors: List<LureColor>,
    val lures: List<Lure>,
    val lurePrimaryColorCrossRef: List<LurePrimaryColorCrossRef>,
    val lureSecondaryColorCrossRef: List<LureSecondaryColorCrossRef>,
    val lureGlowColorCrossRef: List<LureGlowColorCrossRef>,
    val tackleboxes: List<TackleBox>,
    val tackleBoxLureCrossRef: List<TackleBoxLureCrossRef>,
    val species: List<Species>,
    val fish: List<Fish>,
    val photos: List<Photo>,
    val photoEventsCrossRef: List<PhotoEventCrossRef>,
    val photoFishCrossRef: List<PhotoFishCrossRef>,
    val photoFishermanCrossRef: List<PhotoFishermanCrossRef>,
    val photoLureCrossRef: List<PhotoLureCrossRef>,
    val photoSpeciesCrossRef: List<PhotoSpeciesCrossRef>,
    val photoTripCrossRef: List<PhotoTripCrossRef>
)

class MainViewModel(
    private val locationProvider: LocationProvider,
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val eventDao: EventDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {
    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    // Call this whenever the app starts up or right after the permission dialog closes
    fun refreshPermissionStatus() {
        _hasLocationPermission.value = locationProvider.hasLocationPermission()
    }

    private val json = Json { prettyPrint = true }
    private val json_import = Json { ignoreUnknownKeys = true }

    private val _selectEvent = MutableStateFlow(0)
    val selectEvent = _selectEvent.asStateFlow()

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()

    val species: Flow<List<Species>> = fishDao.getAllSpecies()
    val allFish: Flow<List<FishWithDetails>> =
        fishDao.getFishWithDetails(null, null, null, null)

    fun getSegmentsForTrip(tripId: String): Flow<List<Event>> {
        return eventDao.getEventsForTrip(tripId)
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForEvent(segmentId)
    }

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
                    lurePrimaryColorCrossRef = lureDao.getAllLurePrimaryColorCrossRefs().firstOrNull() ?: emptyList(),
                    lureSecondaryColorCrossRef = lureDao.getAllLureSecondaryColorCrossRefs().firstOrNull() ?: emptyList(),
                    lureGlowColorCrossRef = lureDao.getAllLureGlowColorCrossRefs().firstOrNull() ?: emptyList(),
                    tackleboxes = tackleBoxDao.getAllTackleBoxes().firstOrNull() ?: emptyList(),
                    tackleBoxLureCrossRef = tackleBoxDao.getAllTackleBoxLureCrossRefs().firstOrNull() ?: emptyList(),
                    species = fishDao.getAllSpecies().firstOrNull() ?: emptyList(),
                    fish = fishDao.getAllFish().firstOrNull() ?: emptyList(),
                    photos = photoDao.getAllPhotos().firstOrNull() ?: emptyList(),
                    photoEventsCrossRef = photoDao.getAllPhotoEventCrossRefs().firstOrNull() ?: emptyList(),
                    photoFishCrossRef = photoDao.getAllPhotoFishCrossRefs().firstOrNull() ?: emptyList(),
                    photoFishermanCrossRef = photoDao.getAllPhotoFishermanCrossRefs().firstOrNull() ?: emptyList(),
                    photoLureCrossRef = photoDao.getAllPhotoLureCrossRefs().firstOrNull() ?: emptyList(),
                    photoSpeciesCrossRef = photoDao.getAllPhotoSpeciesCrossRefs().firstOrNull() ?: emptyList(),
                    photoTripCrossRef = photoDao.getAllPhotoTripCrossRefs().firstOrNull() ?: emptyList()
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
                    lureDao.deleteAllLureColors()
                    lureDao.deleteAllLures()
                    lureDao.deleteAllLurePrimaryColorCrossRefs()
                    lureDao.deleteAllLureSecondaryColorCrossRefs()
                    lureDao.deleteAllLureGlowColorCrossRefs()
                    tackleBoxDao.deleteAllTackleBoxes()
                    tackleBoxDao.deleteAllTackleBoxLureCrossRefs()
                    fishDao.deleteAllSpecies()
                    fishDao.deleteAllFish()
                    photoDao.deleteAllPhotos()
                    photoDao.deleteAllPhotoEventCrossRefs()
                    photoDao.deleteAllPhotoFishCrossRefs()
                    photoDao.deleteAllPhotoFishermanCrossRefs()
                    photoDao.deleteAllPhotoLureCrossRefs()
                    photoDao.deleteAllPhotoSpeciesCrossRefs()
                    photoDao.deleteAllPhotoTripCrossRefs()

                    data.trips.forEach { tripDao.insertTrip(it) }
                    data.events.forEach { eventDao.insertEvent(it) }
                    data.fishermen.forEach { fishermanDao.insertFisherman(it) }
                    data.tackleboxes.forEach { tackleBoxDao.insertTackleBox(it) }
                    data.tripFishermanCrossRef.forEach { tripDao.insertCrossRef(it) }
                    data.eventFishermanCrossRef.forEach { eventDao.insertEventFishermanCrossRef(it) }
                    data.colors.forEach { lureDao.insertLureColor(it) }
                    data.lures.forEach { lureDao.insertLure(it) }
                    data.lurePrimaryColorCrossRef.forEach { lureDao.upsertLurePrimaryColorCrossRef(it) }
                    data.lureSecondaryColorCrossRef.forEach { lureDao.upsertLureSecondaryColorCrossRef(it) }
                    data.lureGlowColorCrossRef.forEach { lureDao.upsertLureGlowColorCrossRef(it) }
                    data.tackleBoxLureCrossRef.forEach { tackleBoxDao.insertLureToTackleBox(it) }
                    data.species.forEach { fishDao.insertSpecies(it) }
                    data.fish.forEach { fishDao.insertFish(it) }
                    data.photos.forEach { photoDao.insertPhoto(it) }
                    data.photoEventsCrossRef.forEach { photoDao.addEventPhoto(it) }
                    data.photoFishCrossRef.forEach { photoDao.addFishPhoto(it) }
                    data.photoFishermanCrossRef.forEach { photoDao.addFishermanPhoto(it) }
                    data.photoLureCrossRef.forEach { photoDao.addLurePhoto(it) }
                    data.photoSpeciesCrossRef.forEach { photoDao.addSpeciesPhoto(it) }
                    data.photoTripCrossRef.forEach { photoDao.addTripPhoto(it) }
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
    private val locationProvider: LocationProvider,
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
            return MainViewModel(
                locationProvider,
                tripDao,
                fishermanDao,
                eventDao,
                lureDao,
                fishDao,
                photoDao,
                tackleBoxDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}