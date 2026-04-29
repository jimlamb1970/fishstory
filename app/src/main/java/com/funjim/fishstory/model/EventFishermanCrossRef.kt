package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

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
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
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
data class EventFishermanCrossRef(
    val eventId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)
