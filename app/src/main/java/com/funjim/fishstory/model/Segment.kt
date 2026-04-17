package com.funjim.fishstory.model

import androidx.room.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "segment_table",
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
data class Segment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val name: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class SegmentWithFishermen(
    @Embedded val segment: Segment,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SegmentFishermanCrossRef::class,
            parentColumn = "segmentId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>
)

data class SegmentWithDetails(
    @Embedded val segment: Segment,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SegmentFishermanCrossRef::class,
            parentColumn = "segmentId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>,
    @Relation(
        parentColumn = "id",
        entityColumn = "segmentId"
    )
    val fish: List<Fish>
)

// TODO - rename values to better match TripSummary
data class SegmentSummary(
    @Embedded val segment: Segment,
    val fishermanCount: Int,
    val tackleBoxCount: Int,
    val fishCaught: Int,
    val fishKept: Int,
    val biggestFish: String?,
    val mostCaughtName: String?,
    val mostCaught: Int
)