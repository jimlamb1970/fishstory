package com.funjim.fishstory.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.collections.List

// Placeholder data structure for serialization example. Replace with actual DB entities.
@Serializable
data class DatabaseExportData(
    val trips: List<Trip>,
    val segments: List<Segment>,
    val fishermen: List<Fisherman>,
    val tripFishermanCrossRef: List<TripFishermanCrossRef>,
    val segmentFishermanCrossRef: List<SegmentFishermanCrossRef>,
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
    private val segmentDao: SegmentDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {
    // A simple flow to broadcast volume key directions: 1 for Up, -1 for Down
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

    private val _deviceLocation = MutableStateFlow<android.location.Location?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()

    fun fetchDeviceLocationOnce(context: Context) {
        if (_deviceLocation.value != null) return // already fetched, do nothing
        viewModelScope.launch {
            _deviceLocation.value = getCurrentLocation(context)
        }
    }

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()

    val lureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()
    val species: Flow<List<Species>> = fishDao.getAllSpecies()
    val activeSegments: Flow<List<Segment>> = segmentDao.getCurrentActiveSegments()
    val allFish: Flow<List<FishWithDetails>> = fishDao.getAllFishWithDetails()

    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
    }

    fun removeTripFishermenNotInSet(tripId: String, newSet: Set<String>) {
        viewModelScope.launch {
            tripDao.removeFishermenNotInSet(tripId, newSet)
        }
    }

    fun removeSegmentFishermenNotInSet(segmentId: String, newSet: Set<String>) {
        viewModelScope.launch {
            segmentDao.removeFishermenNotInSet(segmentId, newSet)
        }
    }

    fun syncTripFishermen(tripId: String, newSet: Set<String>) {
        viewModelScope.launch {
            // 1. Get the CURRENT state from the DB (one-time fetch)
            val currentTrip = tripDao.getTripWithFishermen(tripId).firstOrNull()
            if (currentTrip != null) {
                val currentSet = currentTrip.fishermen.map { it.id }.toSet()

                // 2. Calculate the Delta
                val toAdd = newSet - currentSet       // Present in new, missing in DB
                val toRemove = currentSet - newSet    // Present in DB, missing in new

                // 3. Apply changes (Wrapped in a transaction in the Repository/DAO)
                toRemove.forEach { id ->
                    deleteFishermanFromTrip(tripId, id)
                }

                toAdd.forEach { id ->
                    addFishermanToTrip(tripId, id)
                }
            }
        }
    }

    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?> {
        return tripDao.getTripWithDetails(tripId)
    }

    suspend fun addTrip(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun upsertTrip(trip: Trip) {
        tripDao.upsertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun deleteTripById(tripId: String) {
        tripDao.deleteTripById(tripId)
    }

    suspend fun addFisherman(firstName: String, lastName: String, nickname: String) {
        // Check if the fisherman already exists
        val fishermanByName = fishermanDao.getFishermanByName(firstName, lastName, nickname)

        // If the fisherman does not exist, add the fisherman and also create
        // a tacklebox for the fisherman
        if (fishermanByName == null) {
            val fisherman = Fisherman(firstName = firstName, lastName = lastName, nickname = nickname)
            fishermanDao.insert(fisherman)
            // Automatically create a tackle box for the new fisherman
            tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fisherman.id))
        }
    }

    suspend fun addFisherman(fisherman: Fisherman) {
        // Check if the fisherman already exists
        val fishermanByName = fishermanDao.getFishermanByName(fisherman.firstName, fisherman.lastName, fisherman.nickname)

        // If the fisherman does not exist, add the fisherman and also create
        // a tacklebox for the fisherman
        if (fishermanByName == null) {
            fishermanDao.insert(fisherman)
            // Automatically create a tackle box for the new fisherman
            tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fisherman.id))
        }
    }

    suspend fun updateFisherman(fisherman: Fisherman) {
        fishermanDao.update(fisherman)
    }

    suspend fun deleteFisherman(fisherman: Fisherman) {
        fishermanDao.deleteFisherman(fisherman)
    }

    suspend fun addFishermanToTrip(tripId: String, fishermanId: String) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.insertCrossRef(crossRef)
    }

    suspend fun deleteFishermanFromTrip(tripId: String, fishermanId: String) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.deleteCrossRef(crossRef)
    }

    suspend fun deleteTripFromFisherman(tripId: String, fishermanId: String) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.deleteCrossRef(crossRef)
    }

    fun getFishermanWithTrips(fishermanId: String): Flow<FishermanWithTrips?> {
        return fishermanDao.getFishermanWithTrips(fishermanId)
    }

    fun getFishermanWithDetails(fishermanId: String): Flow<FishermanWithDetails?> {
        return fishermanDao.getFishermanWithDetails(fishermanId)
    }

    suspend fun addSegment(segment: Segment) {
        segmentDao.insertSegment(segment)
    }

    suspend fun upsertSegment(segment: Segment) {
        segmentDao.upsertSegment(segment)
    }

    fun upsertSegmentFishermanCrossRef(segmentId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            segmentDao.upsertSegmentFishermanCrossRef(
                SegmentFishermanCrossRef(segmentId, fishermanId, tackleBoxId)
            )
        }
    }

    suspend fun upsertSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef) {
        segmentDao.upsertSegmentFishermanCrossRef(crossRef)
    }

    suspend fun addSegmentWithFishermen(segment: Segment, fishermanIds: Collection<String>) {
        segmentDao.insertSegment(segment)
        fishermanIds.forEach { fid ->
            segmentDao.insertSegmentFishermanCrossRef(SegmentFishermanCrossRef(segment.id, fid))
        }
    }

    suspend fun updateSegment(segment: Segment) {
        segmentDao.updateSegment(segment)
    }

    suspend fun deleteSegment(segment: Segment) {
        segmentDao.deleteSegment(segment)
    }

    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>> {
        return segmentDao.getSegmentsForTrip(tripId)
    }

    fun getSegmentsWithDetailsForTrip(tripId: String): Flow<List<SegmentWithDetails>> {
        return segmentDao.getSegmentsWithDetailsForTrip(tripId)
    }

    fun getSegmentWithFishermen(segmentId: String): Flow<SegmentWithFishermen?> {
        return segmentDao.getSegmentWithFishermen(segmentId)
    }

    fun getSegmentWithDetails(segmentId: String): Flow<SegmentWithDetails?> {
        return segmentDao.getSegmentWithDetails(segmentId)
    }

    suspend fun addFishermanToSegment(segmentId: String, fishermanId: String) {
        segmentDao.insertSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
    }

    suspend fun deleteFishermanFromSegment(segmentId: String, fishermanId: String) {
        segmentDao.deleteSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLuresForFisherman(fishermanId: String, segmentId: String): Flow<List<Lure>> {
        // 1. Create a flow that emits the tackleBoxId
        // Note: This assumes segmentDao returns a Flow.
        // If it's a suspend function, we wrap it in a flow { ... }
        return segmentDao.getSegmentFishermanTackleBoxId(segmentId, fishermanId)
            .flatMapLatest { tackleBoxId ->
                if (tackleBoxId != null) {
                    tackleBoxDao.getLuresInTackleBox(tackleBoxId)
                } else {
                    // If no box is found, return an empty list stream
                    flowOf(emptyList())
                }
            }
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun initializeNewTrip(): String {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            // Create a blank trip record immediately
            tripDao.insertTrip(
                Trip(
                    id = newId,
                    name = "", // User fills this in Step 1
                    startDate = System.currentTimeMillis()
                )
            )
        }
        return newId
    }

    fun getTripFishermanCrossRefs(tripId: String): Flow<List<TripFishermanCrossRef>> {
        return tripDao.getTripFishermanCrossRefs(tripId)
    }

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return tackleBoxDao.getTackleBoxesForFisherman(fishermanId)
    }

    fun upsertTripFishermanCrossRef(tripId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripDao.upsertTripFishermanCrossRef(
                TripFishermanCrossRef(tripId, fishermanId, tackleBoxId)
            )
        }
    }

    fun createAndAssignTackleBox(fishermanId: String, name: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            tackleBoxDao.insertTackleBox(tackleBox)
            onCreated(tackleBox.id)
        }
    }

    fun toggleFishermanForTrip(tripId: String, fishermanId: String, isAdded: Boolean) {
        viewModelScope.launch {
            if (isAdded) {
                // Check if they already exist to avoid overwriting an existing tacklebox choice
                val existing = tripDao.getTripFishermanCrossRef(tripId, fishermanId)
                if (existing == null) {
                    tripDao.upsertTripFishermanCrossRef(
                        TripFishermanCrossRef(tripId, fishermanId, null)
                    )
                }
            } else {
                // User unchecked the box, remove the relationship entirely
                tripDao.deleteTripFishermanCrossRef(
                    TripFishermanCrossRef(tripId, fishermanId)
                )
            }
        }
    }
//    fun updateTackleBoxForTripFisherman(tripId: String, fishermanId: String, tackleBoxId: String) {
//        viewModelScope.launch {
//            tripDao.updateTripFishermanTackleBox(TripFishermanCrossRef(tripId, fishermanId, tackleBoxId))
//        }
//    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForSegment(segmentId)
    }

    suspend fun getFishById(id: String): Fish? {
        return fishDao.getFishById(id)
    }

    suspend fun addFish(fish: Fish) {
        fishDao.insertFish(fish)
    }

    suspend fun upsertFish(fish: Fish) {
        return fishDao.upsertFish(fish)
    }

    suspend fun updateFish(fish: Fish) {
        fishDao.updateFish(fish)
    }

    suspend fun deleteFishObject(fish: Fish) {
        fishDao.deleteFish(fish)
    }

    suspend fun addSpecies(species: Species) {
        fishDao.insertSpecies(species)
    }

    suspend fun deleteSpecies(species: Species) {
        fishDao.deleteSpecies(species)
    }

    // Photo operations
    suspend fun addPhoto(photo: Photo) {
        photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    fun getPhotosForTrip(tripId: String): Flow<List<Photo>> = photoDao.getPhotosForTrip(tripId)
    fun getPhotosForSegment(segmentId: String): Flow<List<Photo>> = photoDao.getPhotosForSegment(segmentId)

    val fishPhotos: StateFlow<Map<String, List<Photo>>> = photoDao.getAllFishPhotos()
        .map { photos ->
            photos.filter { it.fishId != null }
                .groupBy { it.fishId!! } // The !! is safe here because of the filter
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

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
                    segments = segmentDao.getAllSegments().firstOrNull() ?: emptyList(),
                    fishermen = fishermanDao.getAllFishermen().firstOrNull() ?: emptyList(),
                    tripFishermanCrossRef = tripDao.getAllTripFishermanCrossRefs().firstOrNull() ?: emptyList(),
                    segmentFishermanCrossRef = segmentDao.getAllSegmentFishermanCrossRefs().firstOrNull() ?: emptyList(),
                    lures = lureDao.getAllLures().firstOrNull() ?: emptyList(),
                    tackleboxes = tackleBoxDao.getAllTackleBoxes().firstOrNull() ?: emptyList(),
                    tackleBoxLureCrossRef = tackleBoxDao.getAllTackleBoxLureCrossRefs().firstOrNull() ?: emptyList(),
                    colors = lureDao.getAllLureColors().firstOrNull() ?: emptyList(),
                    fish = fishDao.getAllFish().firstOrNull() ?: emptyList(),
                    species = fishDao.getAllSpecies().firstOrNull() ?: emptyList(),
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
                    segmentDao.deleteAllSegments()
                    fishermanDao.deleteAllFishermen()
                    tripDao.deleteAllTripFishermanCrossRefs()
                    segmentDao.deleteAllSegmentFishermanCrossRefs()
                    lureDao.deleteAllLures()
                    tackleBoxDao.deleteAllTackleBoxes()
                    tackleBoxDao.deleteAllTackleBoxLureCrossRefs()
                    lureDao.deleteAllLureColors()
                    fishDao.deleteAllFish()
                    fishDao.deleteAllSpecies()
                    photoDao.deleteAllPhotos()

                    data.trips.forEach { tripDao.insertTrip(it) }
                    data.segments.forEach { segmentDao.insertSegment(it) }
                    data.fishermen.forEach { fishermanDao.insertFisherman(it) }
                    data.tripFishermanCrossRef.forEach { tripDao.insertCrossRef(it) }
                    data.segmentFishermanCrossRef.forEach { segmentDao.insertSegmentFishermanCrossRef(it) }
                    data.colors.forEach { lureDao.insertLureColor(it) }
                    data.lures.forEach { lureDao.insertLure(it) }
                    data.tackleboxes.forEach { tackleBoxDao.insertTackleBox(it) }
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
    private val segmentDao: SegmentDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tripDao, fishermanDao, segmentDao, lureDao, fishDao, photoDao, tackleBoxDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}