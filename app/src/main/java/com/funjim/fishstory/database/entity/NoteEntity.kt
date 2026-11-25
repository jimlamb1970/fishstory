package com.funjim.fishstory.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

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
