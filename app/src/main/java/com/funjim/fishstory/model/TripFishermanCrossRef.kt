package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

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
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
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
data class TripFishermanCrossRef(
    val tripId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)
