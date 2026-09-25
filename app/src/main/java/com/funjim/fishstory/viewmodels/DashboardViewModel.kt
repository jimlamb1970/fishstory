package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.EventDetailedSummary
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.LimitEventSummary
import com.funjim.fishstory.model.LimitScope
import com.funjim.fishstory.model.LimitSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripDetailedSummary
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map
import kotlin.collections.plus

class DashboardViewModel(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val currentTime = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    val allSkyConditions: StateFlow<List<SkyCondition>> = envRepo.allSkyConditions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allWaterClarity: StateFlow<List<WaterClarity>> = envRepo.allWaterClarity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    fun refreshPermissionStatus() {
        _hasLocationPermission.value = locationProvider.hasLocationPermission()
    }

    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)

    fun selectEvent(tripId: String, eventId: String) {
        if (_selectedTripId.value != tripId) {
            _selectedTripId.value = tripId
        }
        if (_selectedEventId.value != eventId) {
            _selectedEventId.value = eventId
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTripEvents: StateFlow<EventGroups> = currentTime
        .flatMapLatest { now ->
            tripRepo.getEventsForActiveTrips(now).map { allSegments ->
                // Split them into the 3 groups here
                EventGroups(
                    previous = allSegments.filter { it.event.endTime < now },
                    active = allSegments.filter { now in it.event.startTime..it.event.endTime },
                    upcoming = allSegments.filter { it.event.startTime > now }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EventGroups()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        // Stream group A: Your Trip lists
        tripRepo.getActiveTripSummaries(),
        tripRepo.getUpcomingTrips(),
        tripRepo.getPreviousTripSummaries(),
        // Stream group B: Your dynamic time-sliced event buckets
        activeTripEvents,
        _selectedEventId,
        _selectedTripId
    ) { arrayOfFlowResults ->
        val activeTrips = (arrayOfFlowResults[0] as? List<*>)
            ?.filterIsInstance<TripSummary>()
            ?: emptyList()
        val upcomingTrips = (arrayOfFlowResults[1] as? List<*>)
            ?.filterIsInstance<Trip>()
            ?: emptyList()
        val recentTrips = (arrayOfFlowResults[2] as? List<*>)
            ?.filterIsInstance<TripSummary>()
            ?: emptyList()
        val eventGroups = arrayOfFlowResults[3] as EventGroups
        val selectedEventId = arrayOfFlowResults[4] as String?
        val selectedTripId = arrayOfFlowResults[5] as String?

        // Pass everything downstream as a bundle
        DashboardStateTuple(activeTrips, upcomingTrips, recentTrips, eventGroups, selectedEventId, selectedTripId)

    }.flatMapLatest { tuple ->
        // Resolve which event card should fetch deep database metrics
        val targetEventId = tuple.selectedEventId
            ?: tuple.eventGroups.active.firstOrNull()?.event?.id
        val targetTripId = tuple.selectedTripId
            ?: tuple.eventGroups.active.firstOrNull()?.event?.tripId

        if (tuple.selectedEventId == null && targetEventId != null) {
            _selectedEventId.value = targetEventId
        }
        if (tuple.selectedTripId == null && targetTripId != null) {
            _selectedTripId.value = targetTripId
        }

        if (targetEventId == null || targetTripId == null) {
            // No active events happening right now; emit the data lists immediately
            flowOf(
                DashboardUiState(
                    activeTrips = tuple.activeTrips,
                    upcomingTrips = tuple.upcomingTrips,
                    recentTrips = tuple.recentTrips.take(5),
                    previousEvents = tuple.eventGroups.previous,
                    activeEvents = tuple.eventGroups.active,
                    upcomingEvents = tuple.eventGroups.upcoming,
                    eventSummary = null,
                    tripSummary = null,
                    isLoading = false
                )
            )
        } else {
            // Fetch the fully detailed Database View summary for the active card
            tripRepo.getEventDetailedSummary(targetEventId)
                .combine(tripRepo.getTripDetailedSummary(targetTripId)) { eventSummary, tripSummary ->
                DashboardUiState(
                    activeTrips = tuple.activeTrips,
                    upcomingTrips = tuple.upcomingTrips,
                    recentTrips = tuple.recentTrips.take(5),
                    previousEvents = tuple.eventGroups.previous,
                    activeEvents = tuple.eventGroups.active,
                    upcomingEvents = tuple.eventGroups.upcoming,
                    eventSummary = eventSummary, // Populated stats!
                    tripSummary = tripSummary, // Populated stats!
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val limitSummaries: StateFlow<List<LimitSummary>> = combine(
        _selectedEventId,
        _selectedTripId,
        uiState
    ) { eventId, tripId, state ->
        Triple(eventId, tripId, state)
    }.flatMapLatest { (eventId, tripId, state) ->
        if (eventId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            val eventLimitsFlow = fishRepo.getLimitsForEvent(eventId)
            val tripLimitsFlow = if (tripId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                fishRepo.getLimitsForTrip(tripId)
            }

            val numFishermen = state.eventSummary?.fishermanCount?.coerceAtLeast(1) ?: 1

            combine(
                eventLimitsFlow,
                tripLimitsFlow
            ) { eventLimits, tripLimits ->
                // Map limits to Scoped items
                val scopedEventLimits = eventLimits.map { it to LimitScope.EVENT }
                val scopedTripLimits = tripLimits.map { it to LimitScope.TRIP }

                (scopedEventLimits + scopedTripLimits)
                    .distinctBy { (limit, _) -> limit.id }
            }.flatMapLatest { scopedLimits ->
                if (scopedLimits.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val countFlows = scopedLimits.map { (limit, scope) ->
                        fishRepo.getCaughtCountForLimit(eventId, limit).map { caught ->
                            val singleEventSummary = LimitEventSummary(
                                limit = limit,
                                event = null,
                                fishermanCount = numFishermen,
                                caughtCount = caught
                            )

                            LimitSummary(
                                limit = limit,
                                summaryList = listOf(singleEventSummary),
                                scope = scope
                            )
                        }
                    }
                    combine(countFlows) { summariesArray ->
                        summariesArray.toList()
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun tripPhotos(tripId: String): Flow<List<Photo>> {
        return photoRepo.getPhotosForTrip(tripId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun skyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSkyConditionThumbnail(id)
            .flowOn(Dispatchers.IO)
    }

    fun tripThumbnail(tripId: String): Flow<ByteArray?> {
        return photoRepo.fetchTripThumbnail(tripId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun waterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchWaterClarityThumbnail(id).flowOn(Dispatchers.IO)
    }

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

    fun addWater(water: Water) {
        viewModelScope.launch {
            envRepo.addWater(water)
        }
    }

    fun addWaterClarity(
        item: WaterClarity,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                envRepo.addWaterClarity(item)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _toastMessage.emit("Water Clarity '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding water clarity.")
            }
        }
    }

    fun addWeather(weather: Weather) {
        viewModelScope.launch {
            envRepo.addWeather(weather)
        }
    }

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

    fun addTripPhoto(tripId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addTripPhoto(tripId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }

    fun setTripThumbnail(tripId: String, photoId: String) {
        viewModelScope.launch { photoRepo.setTripThumbnail(tripId, photoId) }
    }

    fun deleteTripPhoto(tripId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteTripPhoto(tripId, photoId) }
    }
}

private data class DashboardStateTuple(
    val activeTrips: List<TripSummary>,
    val upcomingTrips: List<Trip>,
    val recentTrips: List<TripSummary>,
    val eventGroups: EventGroups,
    val selectedEventId: String? = null,
    val selectedTripId: String? = null
)
data class DashboardUiState(
    val activeTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<Trip> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),

    val previousEvents: List<EventSummary> = emptyList(),
    val activeEvents: List<EventSummary> = emptyList(),
    val upcomingEvents: List<EventSummary> = emptyList(),

    val tripSummary: TripDetailedSummary? = null,
    val eventSummary: EventDetailedSummary? = null,

    val isLoading: Boolean = false
)

data class EventGroups(
    val previous: List<EventSummary> = emptyList(),
    val active: List<EventSummary> = emptyList(),
    val upcoming: List<EventSummary> = emptyList()
)

class DashboardViewModelFactory(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                locationProvider,
                envRepo,
                fishRepo,
                photoRepo,
                tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
