package com.funjim.fishstory.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "water_clarity_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class WaterClarityEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
