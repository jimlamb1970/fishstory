package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TackleBox(
    val id: String = UUID.randomUUID().toString(),
    val fishermanId: String,
    val name: String = "My Tackle Box"
)

@Serializable
data class TackleBoxLure(
    val tackleBoxId: String,
    val lureId: String
)

data class TackleBoxWithLures(
    val tackleBox: TackleBox,
    val lures: List<LureWithColors>
)
