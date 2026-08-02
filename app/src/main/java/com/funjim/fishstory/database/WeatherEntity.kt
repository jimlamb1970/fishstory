package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.model.WindDirection
import java.util.UUID
import com.funjim.fishstory.model.Event

@Entity(tableName = "weather_table",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SkyConditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["skyConditionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["skyConditionId"])
    ]
)
data class WeatherEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val temperature: Long? = null,
    val skyConditionId: String? = null,
    val windDirection: WindDirection? = null,
    val windSpeed: Long? = null,
    val atmosphericPressure: Long? = null,
    val airVisibility: Long? = null,
    val airHumidity: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
    )

data class WeatherEntityWithDetails(
    @Embedded
    val weather: WeatherEntity,
    @Relation(
        parentColumn = "skyConditionId",
        entityColumn = "id"
    )
    val skyCondition: SkyConditionEntity
)
