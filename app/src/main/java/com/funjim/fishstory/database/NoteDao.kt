package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.funjim.fishstory.model.NoteFishCrossRef
import com.funjim.fishstory.model.NoteEventCrossRef
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteTripCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripNoteCrossRef(crossRef: NoteTripCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventNoteCrossRef(crossRef: NoteEventCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFishNoteCrossRef(crossRef: NoteFishCrossRef)

    // Transaction to add a note to a specific Trip
    @Transaction
    suspend fun addNoteToTrip(tripId: String, noteText: String) {
        val note = Note(text = noteText)
        insertNote(note)
        insertTripNoteCrossRef(NoteTripCrossRef(tripId = tripId, noteId = note.id))
    }

    // Transaction to add a note to a specific Event
    @Transaction
    suspend fun addNoteToEvent(eventId: String, noteText: String) {
        val note = Note(text = noteText)
        insertNote(note)
        insertEventNoteCrossRef(NoteEventCrossRef(eventId = eventId, noteId = note.id))
    }

    // Transaction to add a note to a specific Fish Catch
    @Transaction
    suspend fun addNoteToFish(fishId: String, noteText: String) {
        val note = Note(text = noteText)
        insertNote(note)
        insertFishNoteCrossRef(NoteFishCrossRef(fishId = fishId, noteId = note.id))
    }

    // Fetch all notes for a specific Trip detail screen
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_trip_cross_ref xr ON n.id = xr.noteId
        WHERE xr.tripId = :tripId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForTrip(tripId: String): Flow<List<Note>>

    // Fetch all notes for a specific Event detail screen
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_event_cross_ref xr ON n.id = xr.noteId
        WHERE xr.eventId = :eventId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForEvent(eventId: String): Flow<List<Note>>

    // Fetch all notes for a specific Fish detail screen
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_fish_cross_ref xr ON n.id = xr.noteId
        WHERE xr.fishId = :fishId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForFish(fishId: String): Flow<List<Note>>
}