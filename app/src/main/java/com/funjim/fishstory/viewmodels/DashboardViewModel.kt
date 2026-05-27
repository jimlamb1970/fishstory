package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.EventDetailedSummary
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripDetailedSummary
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val locationProvider: LocationProvider,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val currentTime = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000)
        }
    }

    private val selectedTripId = MutableStateFlow<String?>(null)
    private val selectedEventId = MutableStateFlow<String?>(null)

    fun selectEvent(tripId: String, eventId: String) {
        if (selectedTripId.value != tripId) {
            selectedTripId.value = tripId
        }
        if (selectedEventId.value != eventId) {
            selectedEventId.value = eventId
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
        selectedEventId,
        selectedTripId
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

    private val _activeTripId = MutableStateFlow<String?>(null)
    fun setActiveTripId(id: String) {
        _activeTripId.value = id
    }

    fun tripThumbnail(tripId: String): Flow<ByteArray?> {
        return photoRepo.fetchTripThumbnail(tripId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
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
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                locationProvider,
                photoRepo,
                tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
