package com.funjim.fishstory.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "water_clarity_table")
data class WaterClarityEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
