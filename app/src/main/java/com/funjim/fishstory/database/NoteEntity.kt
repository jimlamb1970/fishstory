package com.funjim.fishstory.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Trip
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "note_table")
data class NoteEntity(
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
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteTripEntity(
    val tripId: String,
    val noteId: String
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
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEventEntity(
    val eventId: String,
    val noteId: String
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
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteFishEntity(
    val fishId: String,
    val noteId: String
)
