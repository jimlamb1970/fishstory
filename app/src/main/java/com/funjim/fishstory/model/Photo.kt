package com.funjim.fishstory.model

import androidx.room.PrimaryKey
import com.funjim.fishstory.database.entity.SkyConditionPhotoEntity
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
data class PhotoSkyCondition(
    val photoId: String,
    val skyConditionId: String,
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

@Serializable
data class PhotoWaterClarity(
    val photoId: String,
    val waterClarityId: String,
    val isPrimary: Boolean = false
)

interface BasePhoto {
    val id: String
    val uri: String
    val hashcode: String
    val thumbnail: ByteArray?
    val timestamp: Long
    val caption: String?
    val isPrimary: Boolean
    val parentId: String
}

@Serializable
data class BaitPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaitPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class BodyOfWaterPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BodyOfWaterPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class EventPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EventPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class FishPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FishPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class FishermanPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FishermanPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class LurePhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LurePhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class SkyConditionPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkyConditionPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class SpeciesPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpeciesPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class TripPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TripPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
data class WaterClarityPhoto(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    override val uri: String,
    override val hashcode: String,
    override val thumbnail: ByteArray?,
    override val timestamp: Long = System.currentTimeMillis(),
    override val caption: String? = null,
    override val isPrimary: Boolean = false,
    override val parentId: String
) : BasePhoto {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WaterClarityPhoto) return false

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (timestamp != other.timestamp) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}
