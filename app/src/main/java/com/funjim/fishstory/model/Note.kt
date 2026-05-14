package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // GUID string primary key
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Bridge table for Trips
@Entity(
    tableName = "trip_note_cross_ref",
    primaryKeys = ["tripId", "noteId"]
)
data class TripNoteCrossRef(
    val tripId: String, // Maps to trip_table.id
    val noteId: String  // Maps to note_table.id
)

// Bridge table for Events
@Entity(
    tableName = "event_note_cross_ref",
    primaryKeys = ["eventId", "noteId"]
)
data class EventNoteCrossRef(
    val eventId: String, // Maps to event_table.id
    val noteId: String   // Maps to note_table.id
)

// Bridge table for Fish
@Entity(
    tableName = "fish_note_cross_ref",
    primaryKeys = ["fishId", "noteId"]
)
data class FishNoteCrossRef(
    val fishId: String, // Maps to fish_table.id
    val noteId: String  // Maps to note_table.id
)