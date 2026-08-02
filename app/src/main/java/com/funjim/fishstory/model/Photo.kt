package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Photo(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val hashcode: String,
    val thumbnail: ByteArray?,
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Photo

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (timestamp != other.timestamp) return false
        // This is the magic line that compares the actual bytes:
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        // This calculates the hash based on the image content:
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

@Serializable
data class PhotoBait(
    val photoId: String,
    val baitId: String,
    val isPrimary: Boolean = false
)

@Serializable
data class PhotoBodyOfWater(
    val photoId: String,
    val bodyOfWaterId: String,
    val isPrimary: Boolean = false
)

@Serializable
data class PhotoEvent(
    val photoId: String,
    val eventId: String,
    val isPrimary: Boolean = false
)

@Serializable
data class PhotoFish(
    val photoId: String,
    val fishId: String,
    val isPrimary: Boolean = false
)

@Serializable
data class PhotoFisherman(
    val photoId: String,
    val fishermanId: String,
    val isPrimary: Boolean = false
)

@Serializable
data class PhotoLure(
    val photoId: String,
    val lureId: String,
    val isPrimary: Boolean = false
)


@Serializable
data class PhotoSpecies(
    val photoId: String,
    val speciesId: String,
    val isPrimary: Boolean = false
)

@Serializable
data class PhotoTrip(
    val photoId: String,
    val tripId: String,
    val isPrimary: Boolean = false
)
