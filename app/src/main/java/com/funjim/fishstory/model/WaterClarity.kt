package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "water_clarity_table")
data class WaterClarity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
