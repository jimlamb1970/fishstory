package com.funjim.fishstory.repository

import com.funjim.fishstory.database.dao.NoteDao
import com.funjim.fishstory.database.entity.EventNoteEntity
import com.funjim.fishstory.database.entity.FishNoteEntity
import com.funjim.fishstory.database.entity.TripNoteEntity
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toEventNoteDomainList
import com.funjim.fishstory.database.mapper.toFishNoteDomainList
import com.funjim.fishstory.model.EventNote
import com.funjim.fishstory.model.FishNote
import com.funjim.fishstory.model.InsertResult
import com.funjim.fishstory.model.TripNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NoteRepository(
    private val noteDao: NoteDao

) {
    suspend fun addNoteToEvent(eventId: String, content: String) {
        val eventNote = EventNoteEntity(
            parentId = eventId,
            content = content,
        )
        noteDao.insertEventNote(eventNote)
    }
    suspend fun addNoteToFish(fishId: String, content: String) {
        val fishNote = FishNoteEntity(
            parentId = fishId,
            content = content,
        )
        noteDao.insertFishNote(fishNote)
    }
    suspend fun addNoteToTrip(tripId: String, content: String) {
        val tripNote = TripNoteEntity(
            parentId = tripId,
            content = content,
        )
        noteDao.insertTripNote(tripNote)
    }

    suspend fun deleteEventNote(noteId: String) {
        noteDao.deleteEventNoteById(noteId)
    }
    suspend fun deleteFishNote(noteId: String) {
        noteDao.deleteFishNoteById(noteId)
    }
    suspend fun deleteTripNote(noteId: String) {
        noteDao.deleteTripNoteById(noteId)
    }

    suspend fun insertEventNote(note: EventNote) {
        noteDao.upsertEventNote(note.toEntity())
    }
    suspend fun insertFishNote(note: FishNote) {
        noteDao.upsertFishNote(note.toEntity())
    }
    suspend fun insertTripNote(note: TripNote) {
        noteDao.upsertTripNote(note.toEntity())
    }

    suspend fun updateEventNote(id: String, content: String) {
        noteDao.updateEventNoteContent(id, content)
    }
    suspend fun updateFishNote(id: String, content: String) {
        noteDao.updateFishNoteContent(id, content)
    }
    suspend fun updateTripNote(id: String, content: String) {
        noteDao.updateTripNoteContent(id, content)
    }

    fun getNotesForFish(id: String): Flow<List<FishNote>> {
        return noteDao.getFishNotes(id).map { list -> list.toFishNoteDomainList() }
    }

    suspend fun getEventNotes(eventId: String): List<EventNote> {
        return noteDao.getEventNotes(eventId).toEventNoteDomainList()
    }
    suspend fun getFishNotesForEvent(eventId: String): List<FishNote> {
        return noteDao.getFishNotesForEvent(eventId).toFishNoteDomainList()
    }

    suspend fun findOrCreateEventNote(item: EventNote): InsertResult<EventNote> {
        // Check for existing ID and equality
        val existingId = noteDao.getEventNoteById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = noteDao.getEventNoteByData(
                content = item.content,
                timestamp = item.timestamp
            )
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    noteDao.upsertEventNote(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    noteDao.upsertEventNote(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }

    suspend fun findOrCreateFishNote(item: FishNote): InsertResult<FishNote> {
        // Check for existing ID and equality
        val existingId = noteDao.getFishNoteById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = noteDao.getFishNoteByData(
                content = item.content,
                timestamp = item.timestamp
            )
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    noteDao.upsertFishNote(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    noteDao.upsertFishNote(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }
}
