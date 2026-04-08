package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "species_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class Species(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Serializable
@Entity(
    tableName = "fish_table",
    foreignKeys = [
        ForeignKey(
            entity = Species::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Fisherman::class,
            parentColumns = ["id"],
            childColumns = ["fishermanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Segment::class,
            parentColumns = ["id"],
            childColumns = ["segmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Lure::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["speciesId"]),
        Index(value = ["fishermanId"]),
        Index(value = ["tripId"]),
        Index(value = ["segmentId"]),
        Index(value = ["lureId"])
    ]
)
data class Fish(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val speciesId: String?,
    val fishermanId: String?,
    val tripId: String,
    val segmentId: String,
    val lureId: String? = null,
    val length: Double,
    val isReleased: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val holeNumber: Int? = null
)

data class FishWithDetails(
    val id: String,
    val speciesName: String,
    val fishermanName: String,
    val lureName: String?,
    val lurePrimaryColorName: String?,
    val lureSecondaryColorName: String?,
    val lureGlows: Boolean?,
    val lureGlowColorName: String?,
    val length: Double,
    val isReleased: Boolean,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val segmentId: String,
    val segmentName: String,
    val tripId: String,
    val tripName: String,
    val holeNumber: Int? = null
) {
    fun getFullLureName(): String? {
        if (lureName == null) return null
        val sb = StringBuilder(lureName)
        if (!lurePrimaryColorName.isNullOrBlank()) {
            sb.append(" : $lurePrimaryColorName")
        }
        if (lureGlows == true) {
            sb.append(", Glow")
            if (!lureGlowColorName.isNullOrBlank()) {
                sb.append(" : $lureGlowColorName")
            }
        }
        return sb.toString()
    }
}
