package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.FishermanEntity
import com.funjim.fishstory.database.entity.LimitEntity
import com.funjim.fishstory.database.entity.NoteEntity
import com.funjim.fishstory.database.entity.NoteEventEntity
import com.funjim.fishstory.database.entity.NoteFishEntity
import com.funjim.fishstory.database.entity.NoteTripEntity
import com.funjim.fishstory.model.LimitType
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteNoteById(id: String)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    @Upsert
    suspend fun upsertNote(note: NoteEntity)
    @Query("UPDATE note_table SET content = :content WHERE id = :noteId")
    suspend fun updateNoteContent(noteId: String, content: String)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTripNote(crossRef: NoteTripEntity)
    @Upsert
    suspend fun upsertTripNote(crossRef: NoteTripEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEventNote(crossRef: NoteEventEntity)
    @Upsert
    suspend fun upsertEventNote(crossRef: NoteEventEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertFishNote(crossRef: NoteFishEntity)
    @Upsert
    suspend fun upsertFishNote(crossRef: NoteFishEntity)

    @Transaction
    suspend fun addNoteToTrip(tripId: String, noteText: String) {
        val note = NoteEntity(content = noteText)
        insertNote(note)
        insertTripNote(NoteTripEntity(tripId = tripId, noteId = note.id))
    }
    @Transaction
    suspend fun addNoteToEvent(eventId: String, noteText: String) {
        val note = NoteEntity(content = noteText)
        insertNote(note)
        insertEventNote(NoteEventEntity(eventId = eventId, noteId = note.id))
    }
    @Transaction
    suspend fun addNoteToFish(fishId: String, noteText: String) {
        val note = NoteEntity(content = noteText)
        insertNote(note)
        insertFishNote(NoteFishEntity(fishId = fishId, noteId = note.id))
    }

    @Query("SELECT * FROM note_table WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?
    @Query("""
        SELECT * FROM note_table AS n
        WHERE n.content = :content AND n.timestamp = :timestamp
        LIMIT 1
    """)
    suspend fun getNoteByData(
        content: String,
        timestamp: Long
    ): NoteEntity?

    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    suspend fun getAllNotesList(): List<NoteEntity>
    @Query("SELECT * FROM trip_note")
    suspend fun getAllNotesTripList(): List<NoteTripEntity>
    @Query("SELECT * FROM event_note")
    suspend fun getAllNotesEventList(): List<NoteEventEntity>
    @Query("SELECT * FROM fish_note")
    suspend fun getAllNotesFishList(): List<NoteFishEntity>

    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN trip_note xr ON n.id = xr.noteId
        WHERE xr.tripId = :tripId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForTrip(tripId: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT note_table.* FROM note_table
        INNER JOIN event_note ON note_table.id = event_note.noteId
        WHERE event_note.eventId = :eventId
        UNION
        SELECT note_table.* FROM note_table 
        INNER JOIN fish_note ON note_table.id = fish_note.noteId
        INNER JOIN fish_table ON fish_note.fishId = fish_table.id  
        WHERE fish_table.eventId = :eventId
    """)
    suspend fun getAllNotesForEvent(eventId: String): List<NoteEntity>
    @Query("""
        SELECT event_note.* FROM event_note
        WHERE event_note.eventId = :eventId
    """)
    suspend fun getEventNotesForEvent(eventId: String): List<NoteEventEntity>
    @Query("""
        SELECT fish_note.* FROM fish_note 
        INNER JOIN fish_table ON fish_note.fishId = fish_table.id  
        WHERE fish_table.eventId = :eventId
    """)
    suspend fun getFishNotesForEvent(eventId: String): List<NoteFishEntity>

    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN event_note xr ON n.id = xr.noteId
        WHERE xr.eventId = :eventId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForEvent(eventId: String): Flow<List<NoteEntity>>
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN fish_note xr ON n.id = xr.noteId
        WHERE xr.fishId = :fishId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForFish(fishId: String): Flow<List<NoteEntity>>
}