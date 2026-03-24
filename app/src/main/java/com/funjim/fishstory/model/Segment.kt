package com.funjim.fishstory.model

import androidx.room.*

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
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
