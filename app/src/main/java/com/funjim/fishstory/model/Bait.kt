package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "bait_table")
data class Bait(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Serializable
@Entity(
    tableName = "event_bait",
    primaryKeys = ["eventId", "baitId"], // Prevents duplicate target rows
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["baitId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bait::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventBait(
    val eventId: String,
    val baitId: String
)

@Serializable
@Entity(
    tableName = "trip_bait",
    primaryKeys = ["tripId", "baitId"], // Prevents duplicate target rows
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["baitId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bait::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripBait(
    val tripId: String,
    val baitId: String
)