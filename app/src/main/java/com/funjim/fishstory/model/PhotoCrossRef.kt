package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    primaryKeys = ["photoId", "tripId"],
    tableName = "photo_trip_cross_ref",
    indices = [Index(value = ["tripId"])],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
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
data class PhotoTripCrossRef(
    val photoId: String,
    val tripId: String
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "eventId"],
    tableName = "photo_event_cross_ref",
    indices = [Index(value = ["eventId"])],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
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
data class PhotoEventCrossRef(
    val photoId: String,
    val eventId: String
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "lureId"],
    tableName = "photo_lure_cross_ref",
    indices = [Index(value = ["lureId"])],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
            parentColumns = ["id"],
            childColumns = ["photoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Lure::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PhotoLureCrossRef(
    val photoId: String,
    val lureId: String
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "fishermanId"],
    tableName = "photo_fisherman_cross_ref",
    indices = [Index(value = ["fishermanId"])],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
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
data class PhotoFishermanCrossRef(
    val photoId: String,
    val fishermanId: String
)

@Serializable
@Entity(
    primaryKeys = ["photoId", "fishId"],
    tableName = "photo_fish_cross_ref",
    indices = [Index(value = ["fishId"])],
    foreignKeys = [
        ForeignKey(
            entity = Photo::class,
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
data class PhotoFishCrossRef(
    val photoId: String,
    val fishId: String
)