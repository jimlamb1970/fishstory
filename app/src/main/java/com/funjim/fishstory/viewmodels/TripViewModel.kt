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
    EventInfo,        // Step 3 – segment name, dates, location
    EventCrew,        // Step 4 – fishermen + tackle boxes for segment
    Review              // Step 5 – list segments, add another or finish
}

class TripViewModel(
    private val tripRepo: TripRepository,
    private val fishermanRepo: FishermanRepository
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

    // --- (UI State) ---
    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId = _selectedTripId.asStateFlow()
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _draftSegments = MutableStateFlow<List<Event>>(emptyList())
    val draftSegments = _draftSegments.asStateFlow()

    val uiState: StateFlow<TripUiState> = combine(
        tripRepo.getActiveTripSummaries(),
        tripRepo.getUpcomingTripSummaries(),
        tripRepo.getPreviousTripSummaries()
    ) { active, upcoming, previous ->
        TripUiState(
            liveTrips = active,
            upcomingTrips = upcoming,
            recentTrips = previous,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripUiState(isLoading = true)
    )

    // --- Data Streams ---
    private val allTripSummaries = tripRepo.allTripSummaries
    val allTrips = tripRepo.allTrips

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSegmentsForTrip(tripId: String) = tripRepo.getSegmentsForTrip(tripId)

    val fishermen: Flow<List<Fisherman>> = fishermanRepo.allFishermen

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripFishermen: StateFlow<List<Fisherman>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                fishermanRepo.getFishermenForTrip(id)
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
        return tripRepo.getFishermanIdsForTrip(tripId)
    }
    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>> {
        return fishermanRepo.getFishermenForTrip(tripId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventFishermen: StateFlow<List<Fisherman>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                fishermanRepo.getFishermenForSegment(id)
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
        return fishermanRepo.getFishermenForSegment(segmentId)
    }

    fun getTripFishermanTackleBoxId(tripId: String, fishermanId: String): Flow<String?> {
        return tripRepo.getTripFishermanTackleBoxId(tripId, fishermanId)
    }
    fun getSegmentFishermanTackleBoxId(segmentId: String, fishermanId: String): Flow<String?> {
        return tripRepo.getSegmentFishermanTackleBoxId(segmentId, fishermanId)
    }

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepo.getTackleBoxesForFisherman(fishermanId)
    }

    fun getLureCountForTackleBox(tackleBoxId: String?): Flow<Int> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "").map { it.size }
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
    val eventTackleBoxMap: StateFlow<Map<String, String?>> = _selectedEventId
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
        return tripRepo.getTripFishermenTackleBoxIds(tripId)
    }

    fun getSegmentFishermenTackleBoxIds(segmentId: String): Flow<Map<String, String?>> {
        return tripRepo.getFishermanTackleBoxMapping(segmentId)
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
    val eventSummaries: StateFlow<List<EventSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                tripRepo.getSegmentSummaries(id)
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
    val selectedEventSummary: StateFlow<EventSummary?> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                // We watch the master list and filter for the matching ID
                eventSummaries.map { list ->
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
        .flatMapLatest { tripRepo.getPhotosForTrip(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentPhotos: StateFlow<List<Photo>> = _selectedEventId
        .filterNotNull()
        .flatMapLatest { tripRepo.getPhotosForSegment(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TODO -- get this from somehwere else
    var lureColors: Flow<List<LureColor>> = fishermanRepo.allLureColors

    fun getLureNamesInTackleBox(tackleBoxId: String?): Flow<List<String>> {
        val luresFlow = fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "")

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
            tripRepo.upsertTrip(trip)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepo.deleteTripById(trip.id)
        }
    }

    fun deleteTripById(tripId: String) {
        viewModelScope.launch {
            tripRepo.deleteTripById(tripId)
        }
    }

    fun upsertEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.upsertEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.deleteEvent(event)
        }
    }

    fun deleteEventById(eventId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventById(eventId)
        }
    }

    fun upsertTripFishermanCrossRef(tripId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepo.upsertTripFishermanCrossRef(
                TripFishermanCrossRef(tripId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteTripFishermanCrossRef(tripId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepo.deleteTripFishermanCrossRef(TripFishermanCrossRef(tripId, fishermanId))
        }
    }

    fun removeFishermanCrossRefFromTripAndAllEvents(tripId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepo.removeFishermanCrossRefFromTripAndAllSegments(tripId, fishermanId)
        }
    }

    fun upsertEventFishermanCrossRef(eventId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepo.upsertSegmentFishermanCrossRef(
                EventFishermanCrossRef(eventId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteEventFishermanCrossRef(eventId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepo.deleteSegmentFishermanCrossRef(EventFishermanCrossRef(eventId, fishermanId))
        }
    }

    fun removeSegmentFishermenNotInSet(segmentId: String, newSet: Set<String>) {
        viewModelScope.launch {
            tripRepo.removeFishermenNotInSet(segmentId, newSet)
        }
    }

    suspend fun addFisherman(firstName: String, lastName: String, nickname: String) {
        // Check if the fisherman already exists
        val fisherman = fishermanRepo.getFishermanByName(firstName, lastName, nickname)

        // If the fisherman does not exist, add the fisherman (this will also create a tackle box)
        if (fisherman == null) {
            val fisherman = Fisherman(firstName = firstName, lastName = lastName, nickname = nickname)
            fishermanRepo.addFisherman(fisherman)
        }
    }

    fun insertTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
        }
    }

    fun createAndAssignTackleBox(fishermanId: String, tripId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.upsertTripFishermanCrossRef(
                TripFishermanCrossRef(
                    tripId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
    }
    fun createAndAssignEventTackleBox(fishermanId: String, eventId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.upsertSegmentFishermanCrossRef(
                EventFishermanCrossRef(
                    eventId,
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
            tripRepo.addPhoto(photo)
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            tripRepo.deletePhoto(photo)
        }
    }

    fun clearTrip() {
        _selectedTripId.value = null
    }
    fun clearEvent() {
        _selectedEventId.value = null
    }

    fun selectTrip(id: String) {
        _selectedTripId.value = id
    }

    fun selectEvent(id: String) {
        _selectedEventId.value = id
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

    fun clearTripDraft() {
        _tripDraft.value = Trip(id = UUID.randomUUID().toString(), name = "")
    }

    fun updateTripDraft(update: (Trip) -> Trip) {
        _tripDraft.update(update)
        // Synchronize the selected ID for your other flows
        _selectedTripId.value = _tripDraft.value.id
    }

    // --- Segment Draft State ---
    private val _eventDraft = MutableStateFlow(Event(id = UUID.randomUUID().toString(), name = "", tripId = ""))
    val eventDraft = _eventDraft.asStateFlow()

    fun clearEventDraft() {
        _eventDraft.value = Event(id = UUID.randomUUID().toString(), name = "", tripId = "")
    }

    fun updateEventDraft(update: (Event) -> Event) {
        _eventDraft.update(update)
        _selectedEventId.value = _eventDraft.value.id
    }

    // --- Crew Draft State ---
    private val _tripFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val tripFishermenIds = _tripFishermanIds.asStateFlow()

    fun toggleTripFisherman(id: String) {
        _tripFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    private val _segmentFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val eventFishermenIds = _segmentFishermanIds.asStateFlow()

    fun toggleEventFisherman(id: String) {
        _segmentFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun updateEventFishermanIds(ids: Set<String>) {
        _segmentFishermanIds.value = ids
    }
}

data class TripUiState(
    val liveTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<TripSummary> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

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
