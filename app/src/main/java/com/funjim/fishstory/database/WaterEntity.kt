package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.model.Event
import java.util.UUID

@Entity(tableName = "water_table",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WaterClarityEntity::class,
            parentColumns = ["id"],
            childColumns = ["clarityId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["clarityId"])
    ]
)
data class WaterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val eventId: String,
    val depth: Long? = null,
    val temperature: Long? = null,
    val clarityId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
    )

data class WaterEntityWithDetails(
    @Embedded
    val water: WaterEntity,
    @Relation(
        parentColumn = "clarityId",
        entityColumn = "id"
    )
    val clarity: WaterClarityEntity? = null
)

data class WaterSummaryEntity(
    @Embedded val water: WaterEntity,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val targetFishCaught: Int = 0,
    val targetFishKept: Int = 0,
    val largestFish: Long,
    val smallestFish: Long
)
