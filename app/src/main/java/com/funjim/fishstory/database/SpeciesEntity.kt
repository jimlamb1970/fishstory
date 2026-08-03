package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Trip
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "species_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class SpeciesEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

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
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE // If event is deleted, targets wipe out cleanly
        ),
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventTargetSpeciesEntity(
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
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE // If trip is deleted, targets wipe out cleanly
        ),
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripTargetSpeciesEntity(
    val tripId: String,
    val speciesId: String
)

data class SpeciesSummaryEntity(
    @Embedded val species: SpeciesEntity,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val targetFishCaught: Int = 0,
    val targetFishKept: Int = 0,
    val largestFish: Double,
    val smallestFish: Double
)
