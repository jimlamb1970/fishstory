package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "event_target_species",
    primaryKeys = ["eventId", "speciesId"], // Prevents duplicate target rows
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["speciesId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE // If event is deleted, targets wipe out cleanly
        ),
        ForeignKey(
            entity = Species::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventTargetSpecies(
    val eventId: String,
    val speciesId: String
)

@Serializable
@Entity(
    tableName = "trip_target_species",
    primaryKeys = ["tripId", "speciesId"], // Prevents duplicate target rows
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["speciesId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE // If trip is deleted, targets wipe out cleanly
        ),
        ForeignKey(
            entity = Species::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripTargetSpecies(
    val tripId: String,
    val speciesId: String
)