package com.funjim.fishstory.model

import android.app.appsearch.AppSearchSchema
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Water(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val eventId: String,
    val depth: Long? = null,
    val temperature: Long? = null,
    val clarityId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
    )

data class WaterWithDetails(
    val water: Water,
    val clarity: WaterClarity? = null
)
