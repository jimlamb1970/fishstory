package com.funjim.fishstory.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.funjim.fishstory.database.BaitEntity
import com.funjim.fishstory.database.BodyOfWaterEntity
import com.funjim.fishstory.database.LureEntity
import com.funjim.fishstory.database.SpeciesEntity
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Trip
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
    tableName = "photo_bait_cross_ref",
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
    tableName = "photo_body_of_water_cross_ref",
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
    tableName = "photo_event_cross_ref",
    indices = [Index(value = ["eventId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Event::class,
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
    tableName = "photo_fish_cross_ref",
    indices = [Index(value = ["fishId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fish::class,
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
    tableName = "photo_fisherman_cross_ref",
    indices = [Index(value = ["fishermanId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Fisherman::class,
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
    tableName = "photo_lure_cross_ref",
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
    primaryKeys = ["photoId", "speciesId"],
    tableName = "photo_species_cross_ref",
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
    tableName = "photo_trip_cross_ref",
    indices = [Index(value = ["tripId"])],
    foreignKeys = [
        ForeignKey(
            entity = PhotoEntity::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Trip::class,
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
