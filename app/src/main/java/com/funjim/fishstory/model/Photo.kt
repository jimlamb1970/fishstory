package com.funjim.fishstory.model

import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

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
