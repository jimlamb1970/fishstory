package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(tableName = "trip_table")
data class Trip(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class TripWithFishermen(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>
)

data class TripWithDetails(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val segments: List<Segment>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val fish: List<Fish>
)
