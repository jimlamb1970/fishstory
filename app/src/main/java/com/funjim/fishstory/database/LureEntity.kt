package com.funjim.fishstory.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoLureCrossRef
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "lure_color_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class LureColorEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hexCode: String? = null
)

@Serializable
@Entity(tableName = "lure_table")
data class LureEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hookCount: Int = 1,
    val glows: Boolean = false,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
) {
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
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureColorEntity::class,
            parentColumns = ["id"],
            childColumns = ["colorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LurePrimaryColorEntity(
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
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureColorEntity::class,
            parentColumns = ["id"],
            childColumns = ["colorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LureSecondaryColorEntity(
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
            entity = LureEntity::class,
            parentColumns = ["id"],
            childColumns = ["lureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LureColorEntity::class,
            parentColumns = ["id"],
            childColumns = ["colorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LureGlowColorEntity(
    val lureId: String,  // Maps to lure_table.id
    val colorId: String  // Maps to lure_color_table.id
)

data class LureEntityWithPhotos(
    @Embedded val lure: LureEntity,
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

data class LureEntityWithColors(
    @Embedded val lure: LureEntity,

    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LurePrimaryColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val primaryColors: List<LureColorEntity>,

    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureSecondaryColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )

    val secondaryColors: List<LureColorEntity>,
    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureGlowColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val glowColors: List<LureColorEntity>
)

data class LureEntityWithDetails(
    @Embedded val lure: LureEntity,

    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LurePrimaryColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val primaryColors: List<LureColorEntity>,

    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureSecondaryColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )

    val secondaryColors: List<LureColorEntity>,
    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureGlowColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val glowColors: List<LureColorEntity>,

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

data class LureEntityWithColorsSummary(
    @Embedded val lure: LureEntity,

    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LurePrimaryColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val primaryColors: List<LureColorEntity>,
    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureSecondaryColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val secondaryColors: List<LureColorEntity>,
    @Relation(
        entity = LureColorEntity::class,
        parentColumn = "id",        // Lure ID
        entityColumn = "id",        // LureColor ID
        associateBy = Junction(
            value = LureGlowColorEntity::class,
            parentColumn = "lureId",
            entityColumn = "colorId"
        )
    )
    val glowColors: List<LureColorEntity>,

    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val largestFish: Double,
    val smallestFish: Double
)
