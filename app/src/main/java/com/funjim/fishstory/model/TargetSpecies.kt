package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "target_species",
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
            onDelete = ForeignKey.RESTRICT // Can't delete a species if it's an active target
        )
    ]
)
data class TargetSpecies(
    val eventId: String,
    val speciesId: String
)