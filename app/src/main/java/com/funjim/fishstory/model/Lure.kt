package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "lure_color_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class LureColor(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hexCode: String? = null
)

@Serializable
@Entity(tableName = "lure_table")
data class Lure(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hookCount: Int = 1,
    val glows: Boolean = false,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
) {
    fun getDisplayName(): String {
        val sb = StringBuilder(name)

        sb.append(" : Need to fix for colors")

        return sb.toString()
    }
}

@Serializable
@Entity(
    tableName = "lure_primary_color_cross_ref",
    primaryKeys = ["lureId", "colorId"],
    indices = [
        Index(value = ["lureId"]),
        Index(value = ["colorId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Lure::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureColor::class,
            parentColumns = ["id"],
            childColumns = ["colorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LurePrimaryColorCrossRef(
    val lureId: String,  // Maps to lure_table.id
    val colorId: String  // Maps to lure_color_table.id
)

@Serializable
@Entity(
    tableName = "lure_secondary_color_cross_ref",
    primaryKeys = ["lureId", "colorId"],
    indices = [
        Index(value = ["lureId"]),
        Index(value = ["colorId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Lure::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureColor::class,
            parentColumns = ["id"],
            childColumns = ["colorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LureSecondaryColorCrossRef(
    val lureId: String,  // Maps to lure_table.id
    val colorId: String  // Maps to lure_color_table.id
)

@Serializable
@Entity(
    tableName = "lure_glow_color_cross_ref",
    primaryKeys = ["lureId", "colorId"],
    indices = [
        Index(value = ["lureId"]),
        Index(value = ["colorId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Lure::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureColor::class,
            parentColumns = ["id"],
            childColumns = ["colorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LureGlowColorCrossRef(
    val lureId: String,  // Maps to lure_table.id
    val colorId: String  // Maps to lure_color_table.id
)

data class LureWithPhotos(
    @Embedded val lure: Lure,
    @Relation(
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoLureCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
)

data class LureWithColors(
    @Embedded val lure: Lure,

    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LurePrimaryColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val primaryColors: List<LureColor>,

    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureSecondaryColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )

    val secondaryColors: List<LureColor>,
    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureGlowColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val glowColors: List<LureColor>
)

data class LureWithDetails(
    @Embedded val lure: Lure,

    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LurePrimaryColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val primaryColors: List<LureColor>,

    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureSecondaryColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )

    val secondaryColors: List<LureColor>,
    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureGlowColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val glowColors: List<LureColor>,

    @Relation(
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoLureCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
)

data class LureSummary(
    @Embedded val lure: Lure,
    val caughtCount: Int,
    val keptCount: Int,
    val largestFish: Double,
    val smallestFish: Double
)
data class LureSummaryWithColors(
    @Embedded val lure: Lure,

    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LurePrimaryColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val primaryColors: List<LureColor>,

    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureSecondaryColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )

    val secondaryColors: List<LureColor>,
    @Relation(
        entity = LureColor::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureGlowColorCrossRef::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val glowColors: List<LureColor>,

    val caughtCount: Int,
    val keptCount: Int,
    val largestFish: Double,
    val smallestFish: Double
)
