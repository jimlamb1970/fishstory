package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.dao.EventDao
import com.funjim.fishstory.database.entity.EventTargetSpeciesEntity
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.dao.NoteDao
import com.funjim.fishstory.database.dao.TripDao
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toEventNoteDomainList
import com.funjim.fishstory.database.mapper.toEventSummaryDomainList
import com.funjim.fishstory.database.mapper.toFishNoteDomainList
import com.funjim.fishstory.database.mapper.toFishermanDomainList
import com.funjim.fishstory.database.mapper.toNoteDomainList
import com.funjim.fishstory.database.mapper.toSpeciesDomainList
import com.funjim.fishstory.database.mapper.toTripDomainList
import com.funjim.fishstory.database.mapper.toTripSummaryDomainList
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventDetailedSummary
import com.funjim.fishstory.model.EventFisherman
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.EventTargetSpecies
import com.funjim.fishstory.model.EventWithInfo
import com.funjim.fishstory.model.InsertResult
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteEvent
import com.funjim.fishstory.model.NoteFish
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripDetailedSummary
import com.funjim.fishstory.model.TripFisherman
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.TripTargetSpecies
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.model.TripWithFishermen
import com.funjim.fishstory.model.TripWithInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.concurrent.timer

data class AddFishermanResult(
    val addedToTrip: Boolean,
    val addedToEvent: Boolean
)
class TripRepository(
    private val database: FishstoryDatabase,
    private val eventDao: EventDao,
    private val fishermanDao: FishermanDao,
    private val noteDao: NoteDao,
    private val tripDao: TripDao
) {
    // Trip Streams
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
        .map { list -> list.toTripDomainList() }
    val allTripSummaries: Flow<List<TripSummary>> = tripDao.getTripSummaries()
        .map { list -> list.toTripSummaryDomainList() }

    suspend fun getTripById(tripId: String): Trip? {
        return tripDao.getTripById(tripId)?.toDomain()
    }
    fun getTripDetailedSummary(tripId: String): Flow<TripDetailedSummary?> {
        return tripDao.getTripDetailedSummary(tripId)
            .map { it?.toDomain() }
    }
    fun getTripSummary(tripId: String): Flow<TripSummary?> {
        return tripDao.getTripSummary(tripId)
            .map { it?.toDomain() }
    }

    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?> {
        return tripDao.getTripWithDetails(tripId)
            .map { it?.toDomain() }
    }
    fun getTripWithFishermenAndSpecies(tripId: String): Flow<TripWithInfo?> {
        return tripDao.getTripWithFishermenAndSpecies(tripId)
            .map { it?.toDomain() }
    }
    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
            .map { it?.toDomain() }
    }

    fun getActiveTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { now in it.trip.startDate..it.trip.endDate }
            .sortedByDescending { it.trip.startDate }
            .toTripSummaryDomainList()
    }

    /**
     * Upcoming trips are in the future (start date > now).
     */
    fun getUpcomingTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.startDate > now }
            .sortedBy { it.startDate }
            .toTripDomainList()
    }

    fun getUpcomingTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.trip.startDate > now }
            .sortedBy { it.trip.startDate }
            .toTripSummaryDomainList()
    }

    /**
     * Previous trips are in the past (end date < now).
     */
    fun getPreviousTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.endDate < now }
            .sortedByDescending { it.endDate }
            .toTripDomainList()
    }

    fun getPreviousTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.trip.endDate < now}
            .sortedByDescending { it.trip.endDate }
            .toTripSummaryDomainList()
    }

    fun getEventsForActiveTrips(currentTime: Long): Flow<List<EventSummary>> {
        return eventDao.getEventsForActiveTrips(currentTime)
            .map { list -> list.toEventSummaryDomainList() }
    }

    fun getEventSummaries(tripId: String): Flow<List<EventSummary>> {
        return eventDao.getTripEventSummaries(tripId)
            .map { list -> list.toEventSummaryDomainList() }
    }

    suspend fun getEventById(eventId: String): Event? {
        return eventDao.getEventById(eventId)?.toDomain()
    }
    fun getEventDetailedSummary(eventId: String): Flow<EventDetailedSummary?> {
        return eventDao.getEventDetailedSummary(eventId)
            .map { it?.toDomain() }
    }
    fun getEventSummary(eventId: String): Flow<EventSummary?> {
        return eventDao.getEventSummary(eventId)
            .map { it?.toDomain() }
    }
    fun getEventWithDetails(eventId: String): Flow<EventWithDetails?> {
        return eventDao.getEventWithDetails(eventId)
            .map { it?.toDomain() }
    }
    fun getEventWithInfo(eventId: String): Flow<EventWithInfo?> {
        return eventDao.getEventWithInfo(eventId)
            .map { it?.toDomain() }
    }

    // Trip Operations
    suspend fun findOrCreateTrip(trip: Trip): InsertResult<Trip> {
        val existingTrip = tripDao.getTripById(trip.id)
        return if (existingTrip != null) {
            InsertResult(existingTrip.toDomain(), false)
        } else {
            upsertTrip(trip)
            InsertResult(trip, true)
        }
    }

    suspend fun upsertTrip(trip: Trip) {
        tripDao.upsertTrip(trip.toEntity())
    }
    suspend fun deleteTripById(id: String) {
        tripDao.deleteTripById(id)
    }

    // Event Operations
    suspend fun findOrCreateEvent(event: Event): InsertResult<Event> {
        val existing = eventDao.getEventById(event.id)
        return if (existing != null) {
            InsertResult(existing.toDomain(), false)
        } else {
            upsertEvent(event)
            InsertResult(event, true)
        }
    }

    suspend fun upsertEvent(event: Event) {
        eventDao.upsertEvent(event.toEntity())
    }
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event.toEntity())
    }
    suspend fun deleteEventById(id: String) {
        eventDao.deleteEventById(id)
    }

    // Fishermen and TackleBox Operations
    suspend fun upsertTripFisherman(crossRef: TripFisherman) {
        fishermanDao.upsertTripFisherman(crossRef.toEntity())
    }
    suspend fun updateEventFisherman(crossRef: EventFisherman) {
        fishermanDao.upsertEventFisherman(crossRef.toEntity())
    }

    suspend fun addTripFisherman(
        tripId: String,
        fishermanId: String,
        tackleBoxId: String? = null
    ) {
        fishermanDao.addFishermanToTrip(
            tripId = tripId,
            fishermanId = fishermanId,
            tackleBoxId = tackleBoxId
        )
    }

    suspend fun addFishermanToEventAndTrip(
        tripId: String,
        eventId: String,
        fishermanId: String,
        tackleBoxId: String? = null
    ): AddFishermanResult {
        val onTrip = fishermanDao.doesTripFishermanExist(
            tripId = tripId,
            fishermanId = fishermanId
        )

        if (onTrip) {
            fishermanDao.addFishermanToEvent(
                eventId = eventId,
                fishermanId = fishermanId,
                tackleBoxId = tackleBoxId)
        } else {
            fishermanDao.addFishermanToEventAndTrip(
                tripId = tripId,
                eventId = eventId,
                fishermanId = fishermanId,
                tackleBoxId = tackleBoxId)
        }

        return AddFishermanResult(
            addedToTrip = !onTrip,
            addedToEvent = true
        )
    }

    fun getTackleBoxMapForTrip(tripId: String): Flow<Map<String, String?>> =
        fishermanDao.getTripFishermenTackleBoxIds(tripId)
    fun getTackleBoxMapForEvent(eventId: String): Flow<Map<String, String?>> =
        eventDao.getFishermanTackleBoxMapping(eventId)

    fun getTripFishermen(tripId: String): Flow<List<Fisherman>> {
        return fishermanDao.getFishermenForTrip(tripId)
            .map { list -> list.toFishermanDomainList() }
    }
    fun getEventFishermen(eventId: String): Flow<List<Fisherman>> {
        return eventDao.getFishermenForEvent(eventId)
            .map { list -> list.toFishermanDomainList() }
    }

    suspend fun deleteEventFisherman(crossRef: EventFisherman) {
        fishermanDao.deleteEventFisherman(crossRef.toEntity())
    }

    // Target Species
    fun getEventTargetSpecies(eventId: String): Flow<List<Species>> {
        return eventDao.getEventTargetSpecies(eventId).map { speciesList ->
            speciesList.toSpeciesDomainList()
        }
    }
    suspend fun addTargetSpeciesToEvent(
        crossRef: EventTargetSpecies,
        cascade: Boolean = true) {
        database.withTransaction {
            eventDao.upsertEventTargetSpecies(crossRef.toEntity())

            if (cascade) {
                val tripId = eventDao.getTripIdForEvent(crossRef.eventId)
                tripDao.upsertTripTargetSpecies(
                    TripTargetSpecies(
                        tripId = tripId,
                        speciesId = crossRef.speciesId
                    ).toEntity()
                )
            }
        }
    }
    suspend fun deleteEventTargetSpecies(eventId: String, speciesId: String) =
        eventDao.deleteEventTargetSpecies(eventId, speciesId)

    fun getTripTargetSpecies(tripId: String): Flow<List<Species>> {
        return tripDao.getTargetSpeciesForTrip(tripId).map { speciesList ->
            speciesList.toSpeciesDomainList()
        }
    }

    suspend fun addTargetSpeciesToTrip(
        crossRef: TripTargetSpecies,
        cascade: Boolean = true) {
        database.withTransaction {
            tripDao.upsertTripTargetSpecies(crossRef.toEntity())
            if (cascade) {
                val eventIds = eventDao.getEventIdsForTrip(crossRef.tripId)
                if (eventIds.isNotEmpty()) {
                    val eventTargets = eventIds.map { eventId ->
                        EventTargetSpeciesEntity(eventId = eventId, speciesId = crossRef.speciesId)
                    }
                    eventDao.upsertTargetSpeciesForEvents(eventTargets)
                }
            }
        }
    }
    suspend fun deleteTargetSpeciesFromTrip(tripId: String, speciesId: String) {
        database.withTransaction {
            tripDao.deleteTripTargetSpecies(tripId, speciesId)
            val eventIds = eventDao.getEventIdsForTrip(tripId)
            eventDao.deleteTargetSpeciesForEvents(eventIds, speciesId)
        }
    }

    // Note Operations
    suspend fun addNoteToTrip(tripId: String, content: String) {
        noteDao.addNoteToTrip(tripId, content)
    }
    suspend fun addNoteToEvent(eventId: String, content: String) {
        noteDao.addNoteToEvent(eventId, content)
    }
    suspend fun insertEventNote(eventNote: NoteEvent) {
        noteDao.upsertEventNote(eventNote.toEntity())
    }
    suspend fun addNoteToFish(fishId: String, content: String) {
        noteDao.addNoteToFish(fishId, content)
    }
    suspend fun insertFishNote(fishNote: NoteFish) {
        noteDao.upsertFishNote(fishNote.toEntity())
    }
    suspend fun deleteNote(noteId: String) {
        noteDao.deleteNoteById(noteId)
    }
    suspend fun updateNote(
        noteId: String,
        noteText: String) {
        noteDao.updateNoteContent(noteId, noteText)
    }
    fun getAllNotesForEvent(eventId: String): Flow<List<Note>> {
        return noteDao.getAllNotesForEvent(eventId)
            .map { list -> list.toNoteDomainList() }
    }
    fun getEventNotesForEvent(eventId: String): Flow<List<NoteEvent>> {
        return noteDao.getEventNotesForEvent(eventId)
            .map { list -> list.toEventNoteDomainList() }
    }
    fun getFishNotesForEvent(eventId: String): Flow<List<NoteFish>> {
        return noteDao.getFishNotesForEvent(eventId)
            .map { list -> list.toFishNoteDomainList() }
    }
    suspend fun findOrCreateNote(item: Note): InsertResult<Note> {
        // Check for existing ID and equality
        val existingId = noteDao.getNoteById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = noteDao.getNoteByData(
                content = item.content,
                timestamp = item.timestamp
            )
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    noteDao.upsertNote(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    noteDao.upsertNote(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }
}
