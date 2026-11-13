package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TackleBox(
    val id: String = UUID.randomUUID().toString(),
    val fishermanId: String,
    val name: String = "My Tackle Box"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is TackleBox) return false

        return this.name == other.name &&
                this.fishermanId == other.fishermanId
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + fishermanId.hashCode()
        return result
    }
}

@Serializable
data class TackleBoxLure(
    val tackleBoxId: String,
    val lureId: String
)

data class TackleBoxWithLures(
    val tackleBox: TackleBox,
    val lures: List<LureWithColors>
)

data class TackleBoxSummary(
    val tackleBox: TackleBox?,
    val lures: List<LureWithColorsSummary>
)