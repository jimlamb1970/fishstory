package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "segment_fisherman_cross_ref",
    primaryKeys = ["segmentId", "fishermanId"],
    indices = [
        Index(value = ["fishermanId"]),
        Index(value = ["tackleBoxId"])
    ],
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
        ),
            ForeignKey(
            entity = TackleBox::class,
            parentColumns = ["id"],
            childColumns = ["tackleBoxId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class SegmentFishermanCrossRef(
    val segmentId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)
