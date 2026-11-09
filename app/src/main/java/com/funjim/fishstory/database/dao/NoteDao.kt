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

    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_trip_cross_ref xr ON n.id = xr.noteId
        WHERE xr.tripId = :tripId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForTrip(tripId: String): Flow<List<NoteEntity>>

    @Query("""
        SELECT note_table.* FROM note_table
        INNER JOIN note_event_cross_ref ON note_table.id = note_event_cross_ref.noteId
        WHERE note_event_cross_ref.eventId = :eventId
        UNION
        SELECT note_table.* FROM note_table 
        INNER JOIN note_fish_cross_ref ON note_table.id = note_fish_cross_ref.noteId
        INNER JOIN fish_table ON note_fish_cross_ref.fishId = fish_table.id  
        WHERE fish_table.eventId = :eventId
    """)
    suspend fun getAllNotesForEvent(eventId: String): List<NoteEntity>
    @Query("""
        SELECT note_event_cross_ref.* FROM note_event_cross_ref
        WHERE note_event_cross_ref.eventId = :eventId
    """)
    suspend fun getEventNotesForEvent(eventId: String): List<NoteEventEntity>
    @Query("""
        SELECT note_fish_cross_ref.* FROM note_fish_cross_ref 
        INNER JOIN fish_table ON note_fish_cross_ref.fishId = fish_table.id  
        WHERE fish_table.eventId = :eventId
    """)
    suspend fun getFishNotesForEvent(eventId: String): List<NoteFishEntity>

    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_event_cross_ref xr ON n.id = xr.noteId
        WHERE xr.eventId = :eventId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForEvent(eventId: String): Flow<List<NoteEntity>>
    @Transaction
    @Query("""
        SELECT n.* FROM note_table n
        INNER JOIN note_fish_cross_ref xr ON n.id = xr.noteId
        WHERE xr.fishId = :fishId
        ORDER BY n.timestamp DESC
    """)
    fun getNotesForFish(fishId: String): Flow<List<NoteEntity>>
}