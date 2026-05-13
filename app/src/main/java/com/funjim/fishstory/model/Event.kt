package com.funjim.fishstory.model

import androidx.room.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "event_table",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class Event(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val name: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class EventWithPhotos(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",        // Event ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoEventCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
)

data class EventWithFishermen(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventFishermanCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>
)

data class EventWithDetails(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventFishermanCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>,
    @Relation(
        parentColumn = "id",        // Event ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoEventCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>,
    @Relation(
        parentColumn = "id",
        entityColumn = "eventId"
    )
    val fish: List<Fish>
)

data class EventSummary(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "tripId",   // The field in your Event entity
        entityColumn = "id"        // The primary key in your Trip entity
    )
    val trip: Trip,
    val fishCaught: Int,
    val fishKept: Int,
    val fishermanCount: Int,
    val tackleBoxCount: Int,
    val bigFishName: String? = null,
    val bigFishSpecies: String? = null,
    val bigFishLength: Double? = null,
    val mostCaughtName: String? = null,
    val mostCaught: Int? = null
)