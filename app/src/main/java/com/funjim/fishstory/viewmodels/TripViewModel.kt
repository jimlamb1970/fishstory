package com.funjim.fishstory.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.TripRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.collections.get
import kotlin.collections.map
import kotlin.collections.sorted

enum class WizardStep {
    TripInfo,           // Step 1 – name, dates, location
    TripCrew,           // Step 2 – fishermen + tackle boxes for trip
    SegmentInfo,        // Step 3 – segment name, dates, location
    SegmentCrew,        // Step 4 – fishermen + tackle boxes for segment
    Review              // Step 5 – list segments, add another or finish
}

class TripViewModel(
    private val tripRepository: TripRepository,
    private val fishermanRepository: FishermanRepository
) : ViewModel() {
    // --- Location Logic ---
    private val _deviceLocation = MutableStateFlow<android.location.Location?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

    // TODO - can this be replaced by getCurrentLocation in LocationUtils?
    @androidx.annotation.RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    suspend fun getTripCurrentLocation(context: Context): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    fun fetchDeviceLocationOnce(context: Context) {
        if (_deviceLocation.value != null) return

        // 1. Explicitly check if permissions are granted
        val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        // 2. Only launch the coroutine if at least one is granted
        if (hasFineLocation || hasCoarseLocation) {
            viewModelScope.launch {
                try {
                    _deviceLocation.value = getTripCurrentLocation(context)
                } catch (e: SecurityException) {
                    // Handle the case where permission was revoked mid-flight
                    _deviceLocation.value = null
                }
            }
        } else {
            // 3. Optional: Trigger a UI event to ask the user for permission
            println("Location permission not granted")
        }
    }

    // --- Draft Logic (UI State) ---
    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId = _selectedTripId.asStateFlow()
    private val _selectedSegmentId = MutableStateFlow<String?>(null)
    private val _draftSegments = MutableStateFlow<List<Event>>(emptyList())
    val draftSegments = _draftSegments.asStateFlow()

    // --- Data Streams ---
    private val allTripSummaries = tripRepository.allTripSummaries
    val allTrips = tripRepository.allTrips

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSegmentsForTrip(tripId: String) = tripRepository.getSegmentsForTrip(tripId)

    val fishermen: Flow<List<Fisherman>> = fishermanRepository.allFishermen

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripFishermen: StateFlow<List<Fisherman>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                fishermanRepository.getFishermenForTrip(id)
            }
        }
        .map { list ->
            // Sort by Last Name, then First Name
            list.sortedWith(compareBy({ it.fullName }))
        }
        .onEach { list ->
            // SIDE EFFECT: Update the draft IDs whenever the list changes
            // This ensures the wizard state is "set" automatically
            val ids = list.map { it.id }.toSet()
            _tripFishermanIds.value = ids
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getFishermanIdsForTrip(tripId: String): Flow<List<String>> {
        return tripRepository.getFishermanIdsForTrip(tripId)
    }
    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>> {
        return fishermanRepository.getFishermenForTrip(tripId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentFishermen: StateFlow<List<Fisherman>> = _selectedSegmentId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                fishermanRepository.getFishermenForSegment(id)
            }
        }
        .map { list ->
            // Sort by Last Name, then First Name
            list.sortedWith(compareBy({ it.fullName }))
        }
        .onEach { list ->
            // SIDE EFFECT: Update the draft IDs whenever the list changes
            // This ensures the wizard state is "set" automatically
            val ids = list.map { it.id }.toSet()
            _segmentFishermanIds.value = ids
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getFishermenForSegment(segmentId: String): Flow<List<Fisherman>> {
        return fishermanRepository.getFishermenForSegment(segmentId)
    }

    fun getTripFishermanTackleBoxId(tripId: String, fishermanId: String): Flow<String?> {
        return tripRepository.getTripFishermanTackleBoxId(tripId, fishermanId)
    }
    fun getSegmentFishermanTackleBoxId(segmentId: String, fishermanId: String): Flow<String?> {
        return tripRepository.getSegmentFishermanTackleBoxId(segmentId, fishermanId)
    }

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepository.getTackleBoxesForFisherman(fishermanId)
    }

    fun getLureCountForTackleBox(tackleBoxId: String?): Flow<Int> {
        return fishermanRepository.getLuresInTackleBox(tackleBoxId ?: "").map { it.size }
    }

    // TODO -- check flows where filterNotNul is and see if they need to be changed
    @OptIn(ExperimentalCoroutinesApi::class)
    val tripTackleBoxMap: StateFlow<Map<String, String?>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap()) // This clears the map when you set id to null
            } else {
                getTripFishermenTackleBoxIds(tripId = id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // TODO -- check flows where filterNotNul is and see if they need to be changed
    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentTackleBoxMap: StateFlow<Map<String, String?>> = _selectedSegmentId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap()) // This clears the map when you set id to null
            } else {
                getSegmentFishermenTackleBoxIds(segmentId = id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun getTripFishermenTackleBoxIds(tripId: String): Flow<Map<String, String?>> {
        return tripRepository.getTripFishermenTackleBoxIds(tripId)
    }

    fun getSegmentFishermenTackleBoxIds(segmentId: String): Flow<Map<String, String?>> {
        return tripRepository.getSegmentFishermenTackleBoxIds(segmentId)
    }

    // TODO -- add sorting on Trip summaries
    val tripSummaries: StateFlow<List<TripSummary>> = allTripSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTripSummary: StateFlow<TripSummary?> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                // We watch the master list and filter for the matching ID
                tripSummaries.map { list ->
                    list.find { it.trip.id == id }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentSummaries: StateFlow<List<EventSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                tripRepository.getSegmentSummaries(id)
            }
        }
        .map { list ->
            // Sort by whatever property makes sense for your segments
            list.sortedBy { it.event.startTime }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEventSummary: StateFlow<EventSummary?> = _selectedSegmentId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                // We watch the master list and filter for the matching ID
                segmentSummaries.map { list ->
                    list.find { it.event.id == id }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripPhotos: StateFlow<List<Photo>> = _selectedTripId
        .filterNotNull()
        .flatMapLatest { tripRepository.getPhotosForTrip(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentPhotos: StateFlow<List<Photo>> = _selectedSegmentId
        .filterNotNull()
        .flatMapLatest { tripRepository.getPhotosForSegment(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TODO -- get this from somehwere else
    var lureColors: Flow<List<LureColor>> = fishermanRepository.allLureColors

    fun getLureNamesInTackleBox(tackleBoxId: String?): Flow<List<String>> {
        val luresFlow = fishermanRepository.getLuresInTackleBox(tackleBoxId ?: "")

        // Combine the two flows: lures and colors
        return combine(luresFlow, lureColors) { lures, colors ->
            // Create a map for O(1) color lookup performance
            val colorMap = colors.associate { it.id to it.name }

            lures.map { lure ->
                val primary = colorMap[lure.primaryColorId]
                val secondary = colorMap[lure.secondaryColorId]
                val glow = colorMap[lure.glowColorId]

                lure.getDisplayName(primary, secondary, glow)
            }.sorted()
        }
    }


    // --- Actions ---
    fun saveTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.upsertTrip(trip)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip.id)
        }
    }

    fun deleteTripById(tripId: String) {
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
        }
    }

    fun upsertSegment(event: Event) {
        viewModelScope.launch {
            tripRepository.upsertSegment(event)
        }
    }

    fun deleteSegment(event: Event) {
        viewModelScope.launch {
            tripRepository.deleteSegment(event)
        }
    }

    fun deleteSegment(segmentId: String) {
        viewModelScope.launch {
            tripRepository.deleteSegment(segmentId)
        }
    }

    fun upsertTripFishermanCrossRef(tripId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepository.upsertTripFishermanCrossRef(
                TripFishermanCrossRef(tripId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteTripFishermanCrossRef(tripId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepository.deleteTripFishermanCrossRef(TripFishermanCrossRef(tripId, fishermanId))
        }
    }

    fun removeFishermanCrossRefFromTripAndAllSegments(tripId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepository.removeFishermanCrossRefFromTripAndAllSegments(tripId, fishermanId)
        }
    }

    fun upsertSegmentFishermanCrossRef(segmentId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepository.upsertSegmentFishermanCrossRef(
                EventFishermanCrossRef(segmentId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteSegmentFishermanCrossRef(segmentId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepository.deleteSegmentFishermanCrossRef(EventFishermanCrossRef(segmentId, fishermanId))
        }
    }

    fun removeSegmentFishermenNotInSet(segmentId: String, newSet: Set<String>) {
        viewModelScope.launch {
            tripRepository.removeFishermenNotInSet(segmentId, newSet)
        }
    }

    suspend fun addFisherman(firstName: String, lastName: String, nickname: String) {
        // Check if the fisherman already exists
        val fisherman = fishermanRepository.getFishermanByName(firstName, lastName, nickname)

        // If the fisherman does not exist, add the fisherman (this will also create a tackle box)
        if (fisherman == null) {
            val fisherman = Fisherman(firstName = firstName, lastName = lastName, nickname = nickname)
            fishermanRepository.addFisherman(fisherman)
        }
    }

    fun insertTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            fishermanRepository.insertTackleBox(tackleBox)
        }
    }

    fun createAndAssignTackleBox(fishermanId: String, tripId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            fishermanRepository.insertTackleBox(tackleBox)
            tripRepository.upsertTripFishermanCrossRef(
                TripFishermanCrossRef(
                    tripId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
    }
    fun createAndAssignSegmentTackleBox(fishermanId: String, segmentId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            fishermanRepository.insertTackleBox(tackleBox)
            tripRepository.upsertSegmentFishermanCrossRef(
                EventFishermanCrossRef(
                    segmentId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
    }

    fun upsertDraftSegment(updatedEvent: Event) {
        _draftSegments.update { currentList ->
            val alreadyExists = currentList.any { it.id == updatedEvent.id }
            if (alreadyExists) {
                currentList.map { if (it.id == updatedEvent.id) updatedEvent else it }
            } else {
                currentList + updatedEvent
            }
        }
    }

    fun addPhoto(photo: Photo) {
        viewModelScope.launch {
            tripRepository.addPhoto(photo)
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            tripRepository.deletePhoto(photo)
        }
    }

    fun clearTrip() {
        _selectedTripId.value = null
    }
    fun clearSegment() {
        _selectedSegmentId.value = null
    }

    fun selectTrip(id: String) {
        _selectedTripId.value = id
    }

    fun selectSegment(id: String) {
        _selectedSegmentId.value = id
    }

    // TODO - refactor Add Segment logic so that it does not use these drafts
    private val _draftTripStartDate = MutableStateFlow(System.currentTimeMillis())
    val draftTripStartDate = _draftTripStartDate.asStateFlow()

    private val _draftTripEndDate = MutableStateFlow(System.currentTimeMillis())
    val draftTripEndDate = _draftTripEndDate.asStateFlow()

    fun updateDraftTripStartDate(dateMillis: Long) {
        _draftTripStartDate.value = dateMillis
    }

    fun updateDraftTripEndDate(dateMillis: Long) {
        _draftTripEndDate.value = dateMillis
    }

    private val _draftLatitude = MutableStateFlow<Double?>(null)
    val draftLatitude = _draftLatitude.asStateFlow()

    private val _draftLongitude = MutableStateFlow<Double?>(null)
    val draftLongitude = _draftLongitude.asStateFlow()

    fun updateDraftLocation(lat: Double?, lon: Double?) {
        _draftLatitude.value = lat
        _draftLongitude.value = lon
    }

    private val _draftSegmentId = MutableStateFlow("")
    val draftSegmentId = _draftSegmentId.asStateFlow()

    private val _draftSegmentName = MutableStateFlow("")
    val draftSegmentName = _draftSegmentName.asStateFlow()

    private val _draftSegmentStartDate = MutableStateFlow(System.currentTimeMillis())
    val draftSegmentStartDate = _draftSegmentStartDate.asStateFlow()

    private val _draftSegmentEndDate = MutableStateFlow(System.currentTimeMillis())
    val draftSegmentEndDate = _draftSegmentEndDate.asStateFlow()

    private val _draftSegmentLatitude = MutableStateFlow<Double?>(null)
    val draftSegmentLatitude = _draftSegmentLatitude.asStateFlow()

    private val _draftSegmentLongitude = MutableStateFlow<Double?>(null)
    val draftSegmentLongitude = _draftSegmentLongitude.asStateFlow()

    fun clearDraftSegment() {
        _draftSegmentName.value = ""
        val now = System.currentTimeMillis()
        _draftSegmentStartDate.value = now
        _draftSegmentEndDate.value = now
        _draftSegmentLatitude.value = null
        _draftSegmentLongitude.value = null
    }

    fun updateDraftSegmentName(name: String) {
        _draftSegmentName.value = name
    }

    fun updateDraftSegmentStartDate(dateMillis: Long) {
        _draftSegmentStartDate.value = dateMillis
    }

    fun updateDraftSegmentEndDate(dateMillis: Long) {
        _draftSegmentEndDate.value = dateMillis
    }

    fun updateDraftSegmentLocation(lat: Double?, lon: Double?) {
        _draftSegmentLatitude.value = lat
        _draftSegmentLongitude.value = lon
    }

    private val _draftFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val draftFishermanIds = _draftFishermanIds.asStateFlow()

    fun setDraftFisherman(fishermanIds: Set<String>) {
        _draftFishermanIds.update { fishermanIds }
    }

    private val _draftSegmentFishermanIds = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val draftSegmentFishermanIds = _draftSegmentFishermanIds.asStateFlow()

    fun setDraftSegmentFisherman(fishermanIds: Set<String>) {
        _draftSegmentFishermanIds.update { it + (draftSegmentId.value to fishermanIds) }
    }
    fun addDraftSegmentFisherman(segmentId: String, fishermanId: String) {
        _draftSegmentFishermanIds.update { currentMap ->
            val current = currentMap[segmentId] ?: emptySet()
            currentMap + (segmentId to current + fishermanId)
        }
    }

    // --- Wizard Navigation State ---
    private val _currentWizardStep = MutableStateFlow(WizardStep.TripInfo)
    val currentWizardStep = _currentWizardStep.asStateFlow()

    fun updateWizardStep(step: WizardStep) {
        _currentWizardStep.value = step
    }

    // --- Trip Draft State ---
    private val _tripDraft = MutableStateFlow(Trip(id = UUID.randomUUID().toString(), name = ""))
    val tripDraft = _tripDraft.asStateFlow()

    fun updateTripDraft(update: (Trip) -> Trip) {
        _tripDraft.update(update)
        // Synchronize the selected ID for your other flows
        _selectedTripId.value = _tripDraft.value.id
    }

    // --- Segment Draft State ---
    private val _eventDraft = MutableStateFlow(Event(id = UUID.randomUUID().toString(), name = "", tripId = ""))
    val segmentDraft = _eventDraft.asStateFlow()

    fun updateSegmentDraft(update: (Event) -> Event) {
        _eventDraft.update(update)
        _selectedSegmentId.value = _eventDraft.value.id
    }

    // --- Crew Draft State ---
    private val _tripFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val tripFishermanIds = _tripFishermanIds.asStateFlow()

    fun toggleTripFisherman(id: String) {
        _tripFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    private val _segmentFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val segmentFishermanIds = _segmentFishermanIds.asStateFlow()

    fun toggleSegmentFisherman(id: String) {
        _segmentFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun updateSegmentFishermanIds(ids: Set<String>) {
        _segmentFishermanIds.value = ids
    }
}

class TripViewModelFactory(
    private val tripRepository: TripRepository,
    private val fishermanRepository: FishermanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripRepository, fishermanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
