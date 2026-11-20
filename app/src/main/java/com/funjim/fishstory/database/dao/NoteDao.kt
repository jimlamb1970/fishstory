package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.EventNoteEntity
import com.funjim.fishstory.database.entity.FishNoteEntity
import com.funjim.fishstory.database.entity.TripNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Delete
    suspend fun deleteEventNote(note: EventNoteEntity)
    @Query("DELETE FROM event_note_table WHERE id = :id")
    suspend fun deleteEventNoteById(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventNote(note: EventNoteEntity)
    @Upsert
    suspend fun upsertEventNote(note: EventNoteEntity)
    @Query("UPDATE event_note_table SET content = :content WHERE id = :noteId")
    suspend fun updateEventNoteContent(noteId: String, content: String)

    @Delete
    suspend fun deleteFishNote(note: FishNoteEntity)
    @Query("DELETE FROM fish_note_table WHERE id = :id")
    suspend fun deleteFishNoteById(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFishNote(note: FishNoteEntity)
    @Upsert
    suspend fun upsertFishNote(note: FishNoteEntity)
    @Query("UPDATE fish_note_table SET content = :content WHERE id = :noteId")
    suspend fun updateFishNoteContent(noteId: String, content: String)

    @Delete
    suspend fun deleteTripNote(note: TripNoteEntity)
    @Query("DELETE FROM trip_note_table WHERE id = :id")
    suspend fun deleteTripNoteById(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripNote(note: TripNoteEntity)
    @Upsert
    suspend fun upsertTripNote(note: TripNoteEntity)
    @Query("UPDATE trip_note_table SET content = :content WHERE id = :noteId")
    suspend fun updateTripNoteContent(noteId: String, content: String)

    @Query("SELECT * FROM event_note_table WHERE id = :id")
    suspend fun getEventNoteById(id: String): EventNoteEntity?
    @Query("SELECT * FROM fish_note_table WHERE id = :id")
    suspend fun getFishNoteById(id: String): FishNoteEntity?
    @Query("SELECT * FROM trip_note_table WHERE id = :id")
    suspend fun getTripNoteById(id: String): TripNoteEntity?

    @Query("""
        SELECT * FROM event_note_table AS n
        WHERE n.content = :content AND n.timestamp = :timestamp
        LIMIT 1
    """)
    suspend fun getEventNoteByData(
        content: String,
        timestamp: Long
    ): EventNoteEntity?
    @Query("""
        SELECT * FROM fish_note_table AS n
        WHERE n.content = :content AND n.timestamp = :timestamp
        LIMIT 1
    """)
    suspend fun getFishNoteByData(
        content: String,
        timestamp: Long
    ): FishNoteEntity?
    @Query("""
        SELECT * FROM trip_note_table AS n
        WHERE n.content = :content AND n.timestamp = :timestamp
        LIMIT 1
    """)
    suspend fun getTripNoteByData(
        content: String,
        timestamp: Long
    ): TripNoteEntity?

    @Query("SELECT * FROM event_note_table")
    suspend fun getAllEventNotesList(): List<EventNoteEntity>
    @Query("SELECT * FROM fish_note_table")
    suspend fun getAllFishNotesList(): List<FishNoteEntity>
    @Query("SELECT * FROM trip_note_table")
    suspend fun getAllTripNotesList(): List<TripNoteEntity>

    @Query("""
        SELECT event_note_table.* FROM event_note_table
        WHERE parentId = :eventId
        ORDER BY event_note_table.timestamp DESC
    """)
    suspend fun getEventNotes(eventId: String): List<EventNoteEntity>
    @Query("""
        SELECT fish_note_table.* FROM fish_note_table 
        INNER JOIN fish_table ON fish_note_table.parentId = fish_table.id  
        WHERE fish_table.eventId = :eventId
        ORDER BY fish_note_table.timestamp DESC
    """)
    suspend fun getFishNotesForEvent(eventId: String): List<FishNoteEntity>

    @Query("""
        SELECT n.* FROM fish_note_table n
        WHERE parentId = :fishId
        ORDER BY n.timestamp DESC
    """)
    fun getFishNotes(fishId: String): Flow<List<FishNoteEntity>>
}