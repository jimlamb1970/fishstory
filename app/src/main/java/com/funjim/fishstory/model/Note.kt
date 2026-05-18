package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // GUID string primary key
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(
    tableName = "note_trip_cross_ref",
    primaryKeys = ["tripId", "noteId"],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["noteId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteTripCrossRef(
    val tripId: String, // Maps to trip_table.id
    val noteId: String  // Maps to note_table.id
)

@Serializable
@Entity(
    tableName = "note_event_cross_ref",
    primaryKeys = ["eventId", "noteId"],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["noteId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEventCrossRef(
    val eventId: String, // Maps to event_table.id
    val noteId: String   // Maps to note_table.id
)

@Serializable
@Entity(
    tableName = "note_fish_cross_ref",
    primaryKeys = ["fishId", "noteId"],
    indices = [
        Index(value = ["fishId"]),
        Index(value = ["noteId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Fish::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteFishCrossRef(
    val fishId: String, // Maps to fish_table.id
    val noteId: String  // Maps to note_table.id
)
