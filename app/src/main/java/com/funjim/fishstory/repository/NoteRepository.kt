package com.funjim.fishstory.repository

import com.funjim.fishstory.database.dao.NoteDao
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toNoteDomainList
import com.funjim.fishstory.database.mapper.toNoteEventDomainList
import com.funjim.fishstory.database.mapper.toNoteFishDomainList
import com.funjim.fishstory.database.mapper.toNoteTripDomainList
import com.funjim.fishstory.model.InsertResult
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteEvent
import com.funjim.fishstory.model.NoteFish
import com.funjim.fishstory.model.NoteTrip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NoteRepository(
    private val noteDao: NoteDao

) {
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

    fun getNotesForFish(id: String): Flow<List<Note>> {
        return noteDao.getNotesForFish(id).map { list -> list.toNoteDomainList() }
    }

    suspend fun getAllNotesList(): List<Note> {
        return noteDao.getAllNotesList().toNoteDomainList()
    }
    suspend fun getAllNotesTripList(): List<NoteTrip> {
        return noteDao.getAllNotesTripList().toNoteTripDomainList()
    }
    suspend fun getAllNotesEventList(): List<NoteEvent> {
        return noteDao.getAllNotesEventList().toNoteEventDomainList()
    }
    suspend fun getAllNotesFishList(): List<NoteFish> {
        return noteDao.getAllNotesFishList().toNoteFishDomainList()
    }
    suspend fun getAllNotesForEvent(eventId: String): List<Note> {
        return noteDao.getAllNotesForEvent(eventId).toNoteDomainList()
    }
    suspend fun getEventNotesForEvent(eventId: String): List<NoteEvent> {
        return noteDao.getEventNotesForEvent(eventId).toNoteEventDomainList()
    }
    suspend fun getFishNotesForEvent(eventId: String): List<NoteFish> {
        return noteDao.getFishNotesForEvent(eventId).toNoteFishDomainList()
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
