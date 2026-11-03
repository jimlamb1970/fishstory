package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SkyCondition(
    val id: String = UUID.randomUUID().toString(),
    val name: String
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkyCondition) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
