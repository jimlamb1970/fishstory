package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.database.TackleBoxEntity
import com.funjim.fishstory.database.TackleBoxEntityWithLures
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Trip
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "fisherman_table",
    indices = [Index(value = ["firstName", "lastName", "nickname"], unique = true)]
)
data class FishermanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
) {
    @get:Ignore
    val fullName: String
        get() = if (nickname.isNotBlank()) {
            "$firstName \"$nickname\" $lastName".trim()
        } else {
            "$firstName $lastName".trim()
        }
}

data class FishermanEntityWithTrips(
    @Embedded val fisherman: FishermanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanEntity::class,
            parentColumn = "fishermanId",
            entityColumn = "tripId"
        )
    )
    val trips: List<TripEntity>
)

data class FishermanEntityWithDetails(
    @Embedded val fisherman: FishermanEntity,
    @Relation(
        entity = TackleBoxEntity::class,
        parentColumn = "id",
        entityColumn = "fishermanId"
    )
    val tackleBoxWithLures: TackleBoxEntityWithLures?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanEntity::class,
            parentColumn = "fishermanId",
            entityColumn = "tripId"
        )
    )
    val trips: List<TripEntity>
)

data class FishermanEntitySummary(
    @Embedded val fisherman: FishermanEntity,
    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val totalTrips: Int,
    val totalTackleBoxes: Int
)

data class FishermanEntityFullStatistics(
    @Embedded val fisherman: FishermanEntity,

    @Relation(
        entity = TackleBoxEntity::class,
        parentColumn = "id",
        entityColumn = "fishermanId"
    )
    val tackleBoxesWithLures: List<TackleBoxEntityWithLures?>,

    // Fish Extremes
    val largestFishLength: Long?,
    val largestFishTimestamp: Long?,
    val largestFishSpecies: String?,
    val smallestFishLength: Long?,
    val smallestFishTimestamp: Long?,
    val smallestFishSpecies: String?,

    // Best Trip
    val mostTripCatches: Int,
    val bestTripName: String?,
    val bestTripTime: Long?,

    // Best Event & Its Parent Trip
    val mostEventCatches: Int,
    val bestEventName: String?,
    val bestEventTripName: String?,
    val bestEventTime: Long?,

    // Worst Trip
    val fewestTripCatches: Int,
    val worstTripName: String?,
    val worstTripTime: Long?,

    // Worst Event & Its Parent Trip
    val fewestEventCatches: Int,
    val worstEventName: String?,
    val worstEventTripName: String?,
    val worstEventTime: Long?
)

@Serializable
@Entity(
    tableName = "event_fisherman_cross_ref",
    primaryKeys = ["eventId", "fishermanId"],
    indices = [
        Index(value = ["fishermanId"]),
        Index(value = ["tackleBoxId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FishermanEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TackleBoxEntity::class,
            parentColumns = ["id"],
            childColumns = ["tackleBoxId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class EventFishermanEntity(
    val eventId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)

@Serializable
@Entity(
    primaryKeys = ["tripId", "fishermanId"],
    tableName = "trip_fisherman_cross_ref",
    indices = [
        Index(value = ["fishermanId"]),
        Index(value = ["tackleBoxId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FishermanEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TackleBoxEntity::class,
            parentColumns = ["id"],
            childColumns = ["tackleBoxId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TripFishermanEntity(
    val tripId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)
