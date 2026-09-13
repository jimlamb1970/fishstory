package com.funjim.fishstory.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.model.LimitType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "limit_table")
data class LimitEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: LimitType,
    val count: Int = 0,
    val lowerSize: Long? = null,
    val lowerInclusive: Boolean = true,
    val upperSize: Long? = null,
    val upperInclusive: Boolean = true
)

@Serializable
@Entity(
    tableName = "limit_species_table",
    primaryKeys = ["limitId", "speciesId"],
    indices = [
        Index(value = ["limitId"]),
        Index(value = ["speciesId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = LimitEntity::class,
            parentColumns = ["id"],
            childColumns = ["limitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LimitSpeciesEntity(
    val limitId: String,
    val speciesId: String
)

data class LimitWithSpeciesEntity(
    @Embedded val limit: LimitEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LimitSpeciesEntity::class,
            parentColumn = "limitId",
            entityColumn = "speciesId"
        )
    )
    val species: List<SpeciesEntity>
)

@Serializable
@Entity(
    tableName = "event_limit_table",
    primaryKeys = ["eventId", "limitId"],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["limitId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LimitEntity::class,
            parentColumns = ["id"],
            childColumns = ["limitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventLimitEntity(
    val eventId: String,
    val limitId: String
)

@Serializable
@Entity(
    tableName = "trip_limit_table",
    primaryKeys = ["tripId", "limitId"],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["limitId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LimitEntity::class,
            parentColumns = ["id"],
            childColumns = ["limitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripLimitEntity(
    val tripId: String,
    val limitId: String
)
