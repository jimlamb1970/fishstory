package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "water_table",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WaterClarity::class,
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
data class Water(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val depth: Long? = null,
    val temperature: Long? = null,
    val clarityId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
    )

data class WaterWithDetails(
    @Embedded
    val water: Water,
    @Relation(
        parentColumn = "clarityId",
        entityColumn = "id"
    )
    val clarity: WaterClarity? = null
)
