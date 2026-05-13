package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "trip_table")
data class Trip(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class TripWithPhotos(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",        // Trip ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoTripCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
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
        entityColumn = "tripId"
    )
    val events: List<Event>,

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
        parentColumn = "id",        // Trip ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoTripCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>,
 )

data class TripSummary(
    @Embedded val trip: Trip,
    val eventCount: Int = 0,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val fishermanCount: Int = 0,
    val tackleBoxCount: Int = 0,
    val bigFishName: String? = null,
    val bigFishSpecies: String? = null,
    val bigFishLength: Double? = null,
    val mostCaughtName: String? = null,
    val mostCaught: Int? = null
)
