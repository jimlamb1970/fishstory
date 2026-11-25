package com.funjim.fishstory.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
@Entity(
    tableName = "bait_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = BaitEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class BaitPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as BaitPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "body_of_water_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = BodyOfWaterEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class BodyOfWaterPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as BodyOfWaterPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "event_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class EventPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as EventPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "fish_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class FishPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as FishPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "fisherman_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = FishermanEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class FishermanPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as FishermanPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "lure_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class LurePhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as LurePhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "sky_condition_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = SkyConditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class SkyConditionPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as SkyConditionPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "species_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class SpeciesPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as SpeciesPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "trip_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class TripPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as TripPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}

@Serializable
@Entity(
    tableName = "water_clarity_photo_table",
    foreignKeys = [
        ForeignKey(
            entity = WaterClarityEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentId"),
        Index("uri"),
        Index("hashcode")
    ]
)
data class WaterClarityPhotoEntity(
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
        if (javaClass != other?.javaClass) return false

        other as WaterClarityPhotoEntity

        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (uri != other.uri) return false
        if (hashcode != other.hashcode) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        if (caption != other.caption) return false
        if (parentId != other.parentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + hashcode.hashCode()
        result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
        result = 31 * result + (caption?.hashCode() ?: 0)
        result = 31 * result + parentId.hashCode()
        return result
    }
}