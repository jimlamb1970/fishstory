package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "body_of_water_table")
data class BodyOfWater(
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
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BodyOfWater::class,
            parentColumns = ["id"],
            childColumns = ["bodyOfWaterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventBodyOfWater(
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
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BodyOfWater::class,
            parentColumns = ["id"],
            childColumns = ["bodyOfWaterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripBodyOfWater(
    val tripId: String,
    val bodyOfWaterId: String
)