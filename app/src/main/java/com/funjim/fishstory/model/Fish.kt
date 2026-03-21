package com.funjim.fishstory.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "species_table")
data class Species(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val speciesId: Int,
    val fishermanId: Int,
    val tripId: Int,
    val segmentId: Int,
    val lureId: Int? = null,
    val length: Double,
    val isReleased: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class FishWithDetails(
    val id: Int,
    val speciesName: String,
    val fishermanName: String,
    val lureName: String?,
    val lureColorName: String?,
    val lureGlows: Boolean?,
    val lureGlowColorName: String?,
    val length: Double,
    val isReleased: Boolean,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val segmentId: Int,
    val tripId: Int
) {
    fun getFullLureName(): String? {
        if (lureName == null) return null
        val sb = StringBuilder(lureName)
        if (!lureColorName.isNullOrBlank()) {
            sb.append(" : $lureColorName")
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
