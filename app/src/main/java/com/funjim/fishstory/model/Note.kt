package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

interface BaseNote {
    val id: String
    val content: String
    val timestamp: Long
    val parentId: String
}

@Serializable
data class EventNote(
    override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventNote) return false
        return content == other.content &&
                timestamp == other.timestamp &&
                parentId == other.parentId
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class FishNote(
    override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FishNote) return false
        return content == other.content &&
                timestamp == other.timestamp &&
                parentId == other.parentId
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class TripNote(
    override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TripNote) return false
        return content == other.content &&
                timestamp == other.timestamp &&
                parentId == other.parentId
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + parentId.hashCode()
        return result
    }
}


