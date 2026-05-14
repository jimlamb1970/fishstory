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
    tableName = "species_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class Species(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

// TODO -- add the ability to add fish without a trip and/or event
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
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
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
        Index(value = ["eventId"]),
        Index(value = ["lureId"])
    ]
)
data class Fish(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val speciesId: String,
    val fishermanId: String,
    val tripId: String,
    val eventId: String,
    val lureId: String? = null,
    val length: Long? = null,
    val isReleased: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val holeNumber: Int? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class FishWithPhotos(
    @Embedded val fish: Fish,
    @Relation(
        parentColumn = "id",        // Fish ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoFishCrossRef::class,
            parentColumn = "fishId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
)

data class FishWithDetails(
    @Embedded val fish: Fish,

    @Relation(
        parentColumn = "speciesId",
        entityColumn = "id"
    )
    val species: Species,

    @Relation(
        parentColumn = "fishermanId",
        entityColumn = "id"
    )
    val fisherman: Fisherman,

    @Relation(
        parentColumn = "tripId",
        entityColumn = "id"
    )
    val trip: Trip,

    @Relation(
        parentColumn = "eventId",
        entityColumn = "id"
    )
    val event: Event,

    @Relation(
        parentColumn = "lureId",
        entityColumn = "id"
    )
    val lure: Lure?,

    val lurePrimaryColorName: String?,
    val lureSecondaryColorName: String?,
    val lureGlowColorName: String?,

    val photoCount: Int = 0
) {
    val fullLureName: String
        get() {
            if (lure == null) return "No Lure"
            val colors = listOfNotNull(lurePrimaryColorName, lureSecondaryColorName)
                .joinToString("/")
            val glow = if (lure.glows) " (Glow: ${lureGlowColorName})" else ""

            return if (colors.isEmpty()) "${lure.name}$glow" else "${lure.name} ($colors)$glow"
        }
}

data class SpeciesSummary(
    @Embedded val species: Species,
    val caughtCount: Int,
    val keptCount: Int,
    val largestFish: Double,
    val smallest: Double
)

data class FishCounts(
    val totalCaught: Int = 0,
    val totalKept: Int = 0,
    val tripCount: Int = 0,
    val eventCount: Int = 0,
    val fishermanCount: Int = 0,
    val lureCount: Int = 0,
)

data class TripWithCounts(
    @Embedded val trip: Trip,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class EventWithCounts(
    @Embedded val event: Event,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class FishermanWithCounts(
    @Embedded val fisherman: Fisherman,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class SpeciesWithCounts(
    @Embedded val species: Species,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class LureWithCounts(
    @Embedded val lure: LureWithNamesTuple,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class FishSummary(
    val counts: FishCounts = FishCounts(),
//    @Embedded val biggestFish: Fish,
//    @Embedded val smallestFish: Fish,
    val topTrip: TripWithCounts? = null,
    val topEvent: EventWithCounts? = null,
    val topFisherman: FishermanWithCounts? = null,
    val topSpecies: SpeciesWithCounts? = null,
    val topLure: LureWithCounts? = null
)
