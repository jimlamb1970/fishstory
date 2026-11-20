package com.funjim.fishstory.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // GUID string primary key
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoteEntity) return false
        return content == other.content &&
                timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "trip_note",
    primaryKeys = ["tripId", "noteId"],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["noteId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
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
    tableName = "event_note",
    primaryKeys = ["eventId", "noteId"],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["noteId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
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
    tableName = "fish_note",
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

interface BaseNote {
    val id: String
    val content: String
    val timestamp: Long
    val parentId: String
}

@Serializable
@Entity(
    tableName = "event_note_table",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,    // Your parent event entity class
            parentColumns = ["id"],           // The primary key in the Event table
            childColumns = ["parentId"],       // The foreign key column in this note table
            onDelete = ForeignKey.CASCADE     // Automatically deletes this note when the parent event is deleted
        )
    ],
    indices = [
        Index("parentId") // Recommended by Room for performance on foreign key lookups
    ]
)
data class EventNoteEntity(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote

@Serializable
@Entity(
    tableName = "fish_note_table",
    foreignKeys = [
        ForeignKey(
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId")
    ]
)
data class FishNoteEntity(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote

@Serializable
@Entity(
    tableName = "trip_note_table",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId")
    ]
)
data class TripNoteEntity(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote
