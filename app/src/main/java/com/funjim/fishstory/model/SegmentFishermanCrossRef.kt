package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "segment_fisherman_cross_ref",
    primaryKeys = ["segmentId", "fishermanId"],
    foreignKeys = [
        ForeignKey(
            entity = Segment::class,
            parentColumns = ["id"],
            childColumns = ["segmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fisherman::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fishermanId")]
)
data class SegmentFishermanCrossRef(
    val segmentId: String,
    val fishermanId: String
)
