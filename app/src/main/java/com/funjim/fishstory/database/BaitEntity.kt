package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "bait_table")
data class BaitEntity(
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
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BaitEntity::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventBaitEntity(
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
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BaitEntity::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripBaitEntity(
    val tripId: String,
    val baitId: String
)

data class BaitSummaryEntity(
    @Embedded val bait: BaitEntity,
    val fishCaught: Int,
    val fishKept: Int,
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val largestFish: Double,
    val smallestFish: Double
)
