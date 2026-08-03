package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Trip
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "body_of_water_table")
data class BodyOfWaterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
@Entity(
    tableName = "event_body_of_water",
    primaryKeys = ["eventId", "bodyOfWaterId"], // Prevents duplicate target rows
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["bodyOfWaterId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BodyOfWaterEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyOfWaterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventBodyOfWaterEntity(
    val eventId: String,
    val bodyOfWaterId: String
)

@Serializable
@Entity(
    tableName = "trip_body_of_water",
    primaryKeys = ["tripId", "bodyOfWaterId"], // Prevents duplicate target rows
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["bodyOfWaterId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BodyOfWaterEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyOfWaterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripBodyOfWaterEntity(
    val tripId: String,
    val bodyOfWaterId: String
)

data class BodyOfWaterSummaryEntity(
    @Embedded val bodyOfWater: BodyOfWaterEntity,
    val fishCaught: Int,
    val fishKept: Int,
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val largestFish: Double,
    val smallestFish: Double
)
