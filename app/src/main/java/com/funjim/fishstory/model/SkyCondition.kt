package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SkyCondition(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
