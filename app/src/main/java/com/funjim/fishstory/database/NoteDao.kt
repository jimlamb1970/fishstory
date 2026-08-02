package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripNoteCrossRef(crossRef: NoteTripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventNoteCrossRef(crossRef: NoteEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFishNoteCrossRef(crossRef: NoteFishEntity)

    // Transaction to add a note to a specific Trip
    @Transaction
    suspend fun addNoteToTrip(tripId: String, noteText: String) {
        val note = NoteEntity(text = noteText)
        insertNote(note)
        insertTripNoteCrossRef(NoteTripEntity(tripId = tripId, noteId = note.id))
    }

    // Transaction to add a note to a specific Event
    @Transaction
    suspend fun addNoteToEvent(eventId: String, noteText: String) {
        val note = NoteEntity(text = noteText)
        insertNote(note)
        insertEventNoteCrossRef(NoteEventEntity(eventId = eventId, noteId = note.id))
    }

    // Transaction to add a note to a specific Fish Catch
    @Transaction
    suspend fun addNoteToFish(fishId: String, noteText: String) {
        val note = NoteEntity(text = noteText)
        insertNote(note)
        insertFishNoteCrossRef(NoteFishEntity(fishId = fishId, noteId = note.id))
    }

    // Fetch all notes for a specific Trip detail screen
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_trip_cross_ref xr ON n.id = xr.noteId
        WHERE xr.tripId = :tripId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForTrip(tripId: String): Flow<List<NoteEntity>>

    // Fetch all notes for a specific Event detail screen
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_event_cross_ref xr ON n.id = xr.noteId
        WHERE xr.eventId = :eventId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForEvent(eventId: String): Flow<List<NoteEntity>>

    // Fetch all notes for a specific Fish detail screen
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_fish_cross_ref xr ON n.id = xr.noteId
        WHERE xr.fishId = :fishId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForFish(fishId: String): Flow<List<NoteEntity>>
}