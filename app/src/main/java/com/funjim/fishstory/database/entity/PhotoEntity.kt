package com.funjim.fishstory.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.funjim.fishstory.database.entity.WaterClarityEntity
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "photo_table",
    indices = [
        Index(value = ["hashcode"], unique = true)
    ]
)
data class PhotoEntity(
    @PrimaryKey
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

        other as PhotoEntity

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
@Entity(
    primaryKeys = ["photoId", "baitId"],
    tableName = "photo_bait",
    indices = [Index(value = ["baitId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BaitEntity::class,
            parentColumns = ["id"],
            childColumns = ["baitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoBaitEntity(
    val photoId: String,
    val baitId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "bodyOfWaterId"],
    tableName = "photo_body_of_water",
    indices = [Index(value = ["bodyOfWaterId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BodyOfWaterEntity::class,
            parentColumns = ["id"],
            childColumns = ["bodyOfWaterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoBodyOfWaterEntity(
    val photoId: String,
    val bodyOfWaterId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "eventId"],
    tableName = "photo_event",
    indices = [Index(value = ["eventId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoEventEntity(
    val photoId: String,
    val eventId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "fishId"],
    tableName = "photo_fish",
    indices = [Index(value = ["fishId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FishEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoFishEntity(
    val photoId: String,
    val fishId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "fishermanId"],
    tableName = "photo_fisherman",
    indices = [Index(value = ["fishermanId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FishermanEntity::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoFishermanEntity(
    val photoId: String,
    val fishermanId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "lureId"],
    tableName = "photo_lure",
    indices = [Index(value = ["lureId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoLureEntity(
    val photoId: String,
    val lureId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "skyConditionId"],
    tableName = "photo_sky_condition",
    indices = [Index(value = ["skyConditionId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SkyConditionEntity::class,
            parentColumns = ["id"],
            childColumns = ["skyConditionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoSkyConditionEntity(
    val photoId: String,
    val skyConditionId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "speciesId"],
    tableName = "photo_species",
    indices = [Index(value = ["speciesId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpeciesEntity::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoSpeciesEntity(
    val photoId: String,
    val speciesId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "tripId"],
    tableName = "photo_trip",
    indices = [Index(value = ["tripId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoTripEntity(
    val photoId: String,
    val tripId: String,
    val isPrimary: Boolean = false
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "waterClarityId"],
    tableName = "photo_water_clarity",
    indices = [Index(value = ["waterClarityId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WaterClarityEntity::class,
            parentColumns = ["id"],
            childColumns = ["waterClarityId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoWaterClarityEntity(
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