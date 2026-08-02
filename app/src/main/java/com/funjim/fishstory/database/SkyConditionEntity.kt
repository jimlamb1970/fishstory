package com.funjim.fishstory.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sky_condition_table")
data class SkyConditionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
