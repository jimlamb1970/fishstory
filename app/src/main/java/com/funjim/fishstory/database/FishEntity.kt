package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.FishCounts
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Trip
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "fish_table",
    foreignKeys = [
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Fisherman::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = BodyOfWaterEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyOfWaterId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["speciesId"]),
        Index(value = ["fishermanId"]),
        Index(value = ["tripId"]),
        Index(value = ["eventId"]),
        Index(value = ["lureId"]),
        Index(value = ["bodyOfWaterId"])
    ]
)
data class FishEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val speciesId: String,
    val fishermanId: String,
    val tripId: String,
    val eventId: String,
    val caughtCount: Int = 0,
    val keptCount: Int = 0,
    val lureId: String? = null,
    val baitId: String? = null,
    val length: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bodyOfWaterId: String? = null,
    val waterId: String? = null,
    val holeNumber: Int? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class FishEntityWithPhotos(
    @Embedded val fish: FishEntity,
    @Relation(
        parentColumn = "id",        // Fish ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoFishEntity::class,
            parentColumn = "fishId",
            entityColumn = "photoId"
        )
    )
    val photos: List<PhotoEntity>
)

data class FishEntityWithDetails(
    @Embedded val fish: FishEntity,
    @Relation(
        parentColumn = "speciesId",
        entityColumn = "id"
    )
    val species: SpeciesEntity,
    @Relation(
        parentColumn = "fishermanId",
        entityColumn = "id"
    )
    val fisherman: Fisherman,
    @Relation(
        parentColumn = "tripId",
        entityColumn = "id"
    )
    val trip: Trip,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "id"
    )
    val event: Event,
    @Relation(
        entity = LureEntity::class,
        parentColumn = "lureId",
        entityColumn = "id"
    )
    val lure: LureEntityWithColors?,
    @Relation(
        entity = BaitEntity::class,
        parentColumn = "baitId",
        entityColumn = "id"
    )
    val bait: BaitEntity?,
    @Relation(
        entity = BodyOfWaterEntity::class,
        parentColumn = "bodyOfWaterId",
        entityColumn = "id"
    )
    val bodyOfWater: BodyOfWaterEntity?,
    val photoCount: Int = 0
)

data class TripEntityWithCounts(
    @Embedded val trip: Trip,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class EventEntityWithCounts(
    @Embedded val event: Event,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class FishermanEntityWithCounts(
    @Embedded val fisherman: Fisherman,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class SpeciesEntityWithCounts(
    @Embedded val species: SpeciesEntity,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class LureEntityWithCounts(
    @Embedded val lure: LureEntityWithColors,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class FishSummaryEntity(
    val counts: FishCounts = FishCounts(),
    val topTrip: TripEntityWithCounts? = null,
    val topEvent: EventEntityWithCounts? = null,
    val topFisherman: FishermanEntityWithCounts? = null,
    val topSpecies: SpeciesEntityWithCounts? = null,
    val topLure: LureEntityWithCounts? = null
)
